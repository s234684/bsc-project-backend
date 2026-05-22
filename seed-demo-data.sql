-- Demo seed data for the Bachelor Project backend.
--
-- Run from the repository root, for example:
--   psql -U annabelle -d bachelor_project_db -f backend/seed-demo-data.sql
--
-- The backend uses X-Debug-User for mock authentication. Use one of:
--   alice@example.com       PARTICIPANT
--   bob@example.com         PARTICIPANT
--   manager@example.com     MANAGER
--   instructor@example.com  INSTRUCTOR

BEGIN;

-- RLS-protected tables use current_setting('app.current_tenant_id', true).
-- The seed switches this setting before inserting tenant-scoped rows.

INSERT INTO tenants (id, name)
VALUES
  ('2ed04e60-2033-441b-aa15-804420c74cbf', 'Tenant A'),
  ('7b612225-406e-4962-8d1e-ebb25a4a5df8', 'Tenant B')
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name;

-- Remove deterministic demo rows before re-seeding them. This keeps the demo
-- consistent if the seed is run again after changing the fixture data.
SELECT set_config('app.current_tenant_id', '2ed04e60-2033-441b-aa15-804420c74cbf', true);
DELETE FROM gap_profiles WHERE id IN (1, 2, 3, 4);
DELETE FROM responses WHERE id IN (5, 6, 7, 8);

SELECT set_config('app.current_tenant_id', '7b612225-406e-4962-8d1e-ebb25a4a5df8', true);
DELETE FROM gap_profiles WHERE id IN (1, 2, 3, 4);
DELETE FROM responses WHERE id IN (5, 6, 7, 8);

INSERT INTO roles (id, name)
VALUES
  (1, 'PLATFORM_ADMIN'),
  (2, 'MANAGER'),
  (3, 'INSTRUCTOR'),
  (4, 'PARTICIPANT'),
  (5, 'RESEARCHER')
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name;

-- password_hash is required by the schema, but mock authentication only reads
-- the X-Debug-User request header and does not verify the password value.
INSERT INTO users (id, tenant_id, email, password_hash)
VALUES
  (1, '2ed04e60-2033-441b-aa15-804420c74cbf', 'alice@example.com', '123456'),
  (2, '2ed04e60-2033-441b-aa15-804420c74cbf', 'manager@example.com', 'prout'),
  (3, '7b612225-406e-4962-8d1e-ebb25a4a5df8', 'bob@example.com', 'zizi'),
  (4, '2ed04e60-2033-441b-aa15-804420c74cbf', 'instructor@example.com', 'iaminstructor')
ON CONFLICT (id) DO UPDATE
SET
  tenant_id = EXCLUDED.tenant_id,
  email = EXCLUDED.email,
  password_hash = EXCLUDED.password_hash;

INSERT INTO user_roles (role_id, user_id)
VALUES
  (4, 1),
  (2, 2),
  (4, 3),
  (3, 4)
ON CONFLICT (role_id, user_id) DO NOTHING;

-- Tenant A questionnaires
SELECT set_config('app.current_tenant_id', '2ed04e60-2033-441b-aa15-804420c74cbf', true);

INSERT INTO questionnaires (created_by, id, tenant_id, title, definition_json)
VALUES
  (
    NULL,
    1,
    '2ed04e60-2033-441b-aa15-804420c74cbf',
    'Default Security Questionnaire',
    '{
      "questions": [
        { "key": "q1", "text": "Do you use multi-factor authentication?" },
        { "key": "q2", "text": "How often are security updates applied?" },
        { "key": "q3", "text": "Do employees receive security awareness training?" },
        { "key": "q4", "text": "Is sensitive data encrypted at rest?" },
        { "key": "q5", "text": "Do you have an incident response plan?" }
      ]
    }'
  ),
  (
    4,
    3,
    '2ed04e60-2033-441b-aa15-804420c74cbf',
    'Tenant A Practice Questionnaire',
    '{
      "questions": [
        { "key": "practice_q1", "text": "Which security control would you improve first?" },
        { "key": "practice_q2", "text": "What is the main reason for that priority?" }
      ]
    }'
  )
ON CONFLICT (id) DO UPDATE
SET
  created_by = EXCLUDED.created_by,
  tenant_id = EXCLUDED.tenant_id,
  title = EXCLUDED.title,
  definition_json = EXCLUDED.definition_json;

-- Tenant B questionnaires, included to demonstrate tenant isolation.
SELECT set_config('app.current_tenant_id', '7b612225-406e-4962-8d1e-ebb25a4a5df8', true);

INSERT INTO questionnaires (created_by, id, tenant_id, title, definition_json)
VALUES
  (
    NULL,
    2,
    '7b612225-406e-4962-8d1e-ebb25a4a5df8',
    'Tenant B Security Form',
    '{
      "questions": [
        { "key": "q1", "text": "Do you use multi-factor authentication?" },
        { "key": "q2", "text": "How often are security updates applied?" },
        { "key": "q3", "text": "Do employees receive security awareness training?" },
        { "key": "q4", "text": "Is sensitive data encrypted at rest?" }
      ]
    }'
  ),
  (
    NULL,
    4,
    '7b612225-406e-4962-8d1e-ebb25a4a5df8',
    'Tenant B Practice Questionnaire',
    '{
      "questions": [
        { "key": "practice_q1", "text": "What security process should be reviewed next?" },
        { "key": "practice_q2", "text": "Who should be involved in the review?" }
      ]
    }'
  )
ON CONFLICT (id) DO UPDATE
SET
  created_by = EXCLUDED.created_by,
  tenant_id = EXCLUDED.tenant_id,
  title = EXCLUDED.title,
  definition_json = EXCLUDED.definition_json;

-- Tenant A submissions / responses
SELECT set_config('app.current_tenant_id', '2ed04e60-2033-441b-aa15-804420c74cbf', true);

INSERT INTO responses (id, questionnaire_id, user_id, tenant_id, answer_json)
VALUES
  (
    5,
    1,
    1,
    '2ed04e60-2033-441b-aa15-804420c74cbf',
    '{"answers":[{"key":"q1","value":"all the time"},{"key":"q2","value":"weekly"},{"key":"q3","value":"yes"},{"key":"q4","value":"not sure"},{"key":"q5","value":"yes"}]}'
  )
ON CONFLICT (id) DO UPDATE
SET
  questionnaire_id = EXCLUDED.questionnaire_id,
  user_id = EXCLUDED.user_id,
  tenant_id = EXCLUDED.tenant_id,
  answer_json = EXCLUDED.answer_json;

INSERT INTO gap_profiles (
  id,
  tenant_id,
  questionnaire_id,
  participant_id,
  submission_id,
  observed_level,
  target_level,
  gap_value,
  gap_category,
  created_at
)
VALUES
  (
    1,
    '2ed04e60-2033-441b-aa15-804420c74cbf',
    1,
    1,
    5,
    1.9042471798499356,
    5,
    3.0957528201500644,
    'HIGH',
    '2026-04-27 10:26:49.191722'
  )
ON CONFLICT (id) DO UPDATE
SET
  tenant_id = EXCLUDED.tenant_id,
  questionnaire_id = EXCLUDED.questionnaire_id,
  participant_id = EXCLUDED.participant_id,
  submission_id = EXCLUDED.submission_id,
  observed_level = EXCLUDED.observed_level,
  target_level = EXCLUDED.target_level,
  gap_value = EXCLUDED.gap_value,
  gap_category = EXCLUDED.gap_category,
  created_at = EXCLUDED.created_at;

-- Tenant B submitted questionnaire for Bob. Tenant B Practice Questionnaire is
-- intentionally left unanswered so supervisors can submit it through the UI.
SELECT set_config('app.current_tenant_id', '7b612225-406e-4962-8d1e-ebb25a4a5df8', true);

INSERT INTO responses (id, questionnaire_id, user_id, tenant_id, answer_json)
VALUES
  (
    6,
    2,
    3,
    '7b612225-406e-4962-8d1e-ebb25a4a5df8',
    '{"answers":[{"key":"q1","value":"yes"},{"key":"q2","value":"monthly"},{"key":"q3","value":"yes"},{"key":"q4","value":"no"}]}'
  )
ON CONFLICT (id) DO UPDATE
SET
  questionnaire_id = EXCLUDED.questionnaire_id,
  user_id = EXCLUDED.user_id,
  tenant_id = EXCLUDED.tenant_id,
  answer_json = EXCLUDED.answer_json;

INSERT INTO gap_profiles (
  id,
  tenant_id,
  questionnaire_id,
  participant_id,
  submission_id,
  observed_level,
  target_level,
  gap_value,
  gap_category,
  created_at
)
VALUES
  (
    2,
    '7b612225-406e-4962-8d1e-ebb25a4a5df8',
    2,
    3,
    6,
    3.3273224422334664,
    5,
    1.6726775577665336,
    'HIGH',
    '2026-04-27 10:27:37.531648'
  )
ON CONFLICT (id) DO UPDATE
SET
  tenant_id = EXCLUDED.tenant_id,
  questionnaire_id = EXCLUDED.questionnaire_id,
  participant_id = EXCLUDED.participant_id,
  submission_id = EXCLUDED.submission_id,
  observed_level = EXCLUDED.observed_level,
  target_level = EXCLUDED.target_level,
  gap_value = EXCLUDED.gap_value,
  gap_category = EXCLUDED.gap_category,
  created_at = EXCLUDED.created_at;

SELECT setval(pg_get_serial_sequence('users', 'id'), GREATEST((SELECT MAX(id) FROM users), 1), true);
SELECT setval(pg_get_serial_sequence('questionnaires', 'id'), GREATEST((SELECT MAX(id) FROM questionnaires), 1), true);
SELECT setval(pg_get_serial_sequence('responses', 'id'), GREATEST((SELECT MAX(id) FROM responses), 1), true);
SELECT setval(pg_get_serial_sequence('gap_profiles', 'id'), GREATEST((SELECT MAX(id) FROM gap_profiles), 1), true);
SELECT setval(pg_get_serial_sequence('audit_log', 'id'), GREATEST((SELECT MAX(id) FROM audit_log), 1), true);

COMMIT;

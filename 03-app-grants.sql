-- Runtime permissions for the non-superuser Spring Boot application role.
-- audit_log is append-only for annabelle: INSERT/SELECT are allowed, but
-- UPDATE, DELETE, and TRUNCATE are explicitly withheld.

GRANT USAGE ON SCHEMA public TO annabelle;

GRANT SELECT, INSERT, UPDATE, DELETE ON
    tenants,
    roles,
    users,
    user_roles,
    questionnaires,
    responses,
    gap_profiles
TO annabelle;

GRANT SELECT, INSERT ON audit_log TO annabelle;

GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO annabelle;

REVOKE UPDATE, DELETE, TRUNCATE ON audit_log FROM annabelle;

# Bachelor Project Backend

Spring Boot backend for the questionnaire demonstrator. The backend uses PostgreSQL, JPA, and PostgreSQL row-level security (RLS) for tenant isolation.

The frontend authenticates demo users through the `X-Debug-User` HTTP header. There is no password login flow in the demonstrator.

## Demo Users

Use these emails in the frontend login field:

| Email | Role | Tenant |
| --- | --- | --- |
| `alice@example.com` | `PARTICIPANT` | Tenant A |
| `bob@example.com` | `PARTICIPANT` | Tenant B |
| `manager@example.com` | `MANAGER` | Tenant A |
| `instructor@example.com` | `INSTRUCTOR` | Tenant A |
| `platformadmin@example.com` | `PLATFORM_ADMIN` | None |

Seeded participant flow:

- Alice has already submitted `Default Security Questionnaire` and can submit `Tenant A Practice Questionnaire`.
- Bob has already submitted `Tenant B Security Form` and can submit `Tenant B Practice Questionnaire`.

## Option A: Run Everything With Docker Desktop

Requirements:

- Docker Desktop
- The backend repository cloned locally

From the backend repository root:

```bash
docker compose up --build
```

This starts:

- PostgreSQL on `localhost:5432`
- Spring Boot backend on `http://localhost:8080`

On first startup, Docker initializes PostgreSQL with:

- `00-create-app-user.sql`: non-superuser application role for Spring Boot
- `schema.sql`: tables, constraints, and RLS policies
- `seed-demo-data.sql`: tenants, users, roles, questionnaires, responses, and gap profiles
- `03-app-grants.sql`: runtime privileges, including append-only `audit_log` access

The Docker setup uses the official `postgres:16` image for a stable local demonstrator setup.

Database connection details:

```text
Database: bachelor_project_db
Username: annabelle
Password: bachelor_project_password
Host: localhost
Port: 5432
```

The Docker database is initialized by the `postgres` superuser, but the backend
connects as the non-superuser `annabelle` role. This keeps the demonstrator's
append-only audit-log controls meaningful at runtime.

Health check:

```bash
curl http://localhost:8080/api/health
```

To reset the Docker database and reload the schema/seed files:

```bash
docker compose down -v
docker compose up --build
```

## Option B: Run With Local PostgreSQL

Requirements:

- Java 17
- Maven 3.9 or newer
- PostgreSQL
- A database user that can create tables and policies

Create the database:

```bash
createdb -U annabelle bachelor_project_db
```

Load the schema and seed data:

```bash
psql -U annabelle -d bachelor_project_db -f schema.sql
psql -U annabelle -d bachelor_project_db -f seed-demo-data.sql
```

Start the backend:

```bash
mvn spring-boot:run
```

The backend runs at:

```text
http://localhost:8080
```

If your local PostgreSQL credentials differ from the defaults, set environment variables before starting Spring Boot:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/bachelor_project_db
SPRING_DATASOURCE_USERNAME=your_user
SPRING_DATASOURCE_PASSWORD=your_password
mvn spring-boot:run
```

On PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/bachelor_project_db"
$env:SPRING_DATASOURCE_USERNAME="your_user"
$env:SPRING_DATASOURCE_PASSWORD="your_password"
mvn spring-boot:run
```

## RLS Notes

The protected tables are:

- `questionnaires`
- `responses`
- `gap_profiles`

Their RLS policies compare each row's `tenant_id` against:

```sql
current_setting('app.current_tenant_id', true)::uuid
```

The backend sets that value per request in `RlsContextService` using the current mock-authenticated user's tenant. The seed file also sets this value before inserting tenant-scoped rows.

## Audit Log Append-Only Notes

The `audit_log` table is append-only for the `annabelle` application role:

- `annabelle` has `SELECT` and `INSERT` on `audit_log`
- `UPDATE`, `DELETE`, and `TRUNCATE` privileges are revoked from `annabelle`
- row-level triggers block `UPDATE` and `DELETE`
- a statement-level trigger blocks `TRUNCATE` for non-superusers

PostgreSQL superusers can still bypass trigger-based `TRUNCATE`. The Docker
demonstrator avoids that path by running the backend as non-superuser `annabelle`.

## Useful API Checks

With the backend running:

```bash
curl -H "X-Debug-User: alice@example.com" http://localhost:8080/api/auth/me
curl -H "X-Debug-User: instructor@example.com" http://localhost:8080/api/questionnaire
```

## Files for Reproducibility

- `schema.sql`: database schema, constraints, and RLS policies
- `seed-demo-data.sql`: deterministic demo data
- `docker-compose.yaml`: PostgreSQL plus backend service for Docker Desktop
- `Dockerfile`: backend image build

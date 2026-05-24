-- Docker-only bootstrap for the demonstrator database.
-- The official postgres image makes POSTGRES_USER a superuser, so the app
-- role is created separately to keep runtime access non-superuser.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'annabelle') THEN
        CREATE ROLE annabelle LOGIN PASSWORD 'bachelor_project_password'
            NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;
    ELSE
        ALTER ROLE annabelle WITH LOGIN PASSWORD 'bachelor_project_password'
            NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;
    END IF;
END
$$;

GRANT CONNECT ON DATABASE bachelor_project_db TO annabelle;

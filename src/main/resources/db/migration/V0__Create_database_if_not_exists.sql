-- V0__Create_database_if_not_exists.sql
SELECT 'CREATE DATABASE jsonplaceholder_db'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'jsonplaceholder_db');

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

COMMENT ON DATABASE jsonplaceholder_db IS 'Database for JSONPlaceholder API clone with JWT authentication';
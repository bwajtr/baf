-- Testcontainers PostgreSQL initialization script
-- This script sets up the database users and extensions needed for integration tests

-- Create the application database admin:
CREATE USER dbadmin WITH PASSWORD 'dbadmin';

-- Create the application database user:
CREATE USER dbuser WITH PASSWORD 'dbuser';

-- create ext_pgcrypto schema (with owner dbadmin) and add pgcrypto module to it
CREATE SCHEMA ext_pgcrypto AUTHORIZATION dbadmin;
CREATE EXTENSION pgcrypto SCHEMA ext_pgcrypto; -- We will need the pgcrypto module for passwords and hashing
GRANT USAGE ON SCHEMA ext_pgcrypto to dbuser; -- Grant usage also to the dbuser
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA ext_pgcrypto TO dbuser;

-- create btree_gist schema (with owner dbadmin) and add btree_gist extension to it
CREATE SCHEMA btree_gist AUTHORIZATION dbadmin;
CREATE EXTENSION btree_gist SCHEMA btree_gist; -- for easier work with ranges
GRANT USAGE ON SCHEMA btree_gist to dbuser; -- Grant usage also to the dbuser
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA btree_gist TO dbuser;

-- This is to mitigate CVE-2018-1058, see https://wiki.postgresql.org/wiki/A_Guide_to_CVE-2018-1058:_Protect_Your_Search_Path
-- only dbadmin will be allowed to create objects in public schema
REVOKE CREATE ON SCHEMA public FROM PUBLIC;
GRANT CREATE ON SCHEMA public TO dbadmin;
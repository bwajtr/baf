-- Create the application database admin:
CREATE USER dbadmin WITH PASSWORD 'dbadmin';

-- Create the application database user:
CREATE USER dbuser WITH PASSWORD 'dbuser';

-- Create the database:
-- noinspection SqlResolve
CREATE DATABASE primarydb WITH OWNER=dbadmin
                                        LC_COLLATE='en_US.utf8'
                                        LC_CTYPE='en_US.utf8'
                                        ENCODING='UTF8'
                                       TEMPLATE=template0;

-- Connect to the database
\connect primarydb

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

-- Securing application little bit more by preventing dbuser to see some system resources (like activity, function source codes etc.)
-- pg_catalog.pg_proc access control prevents dbuser to see the structure of schemas and source code of functions (which can contain sensitive information)
-- based on this: https://stackoverflow.com/questions/3651720/postgresql-how-can-i-restrict-access-to-code-in-a-function-for-a-user
REVOKE ALL PRIVILEGES ON TABLE pg_catalog.pg_proc FROM dbuser;
REVOKE ALL PRIVILEGES ON TABLE pg_catalog.pg_proc FROM public;
GRANT ALL PRIVILEGES ON TABLE pg_catalog.pg_proc TO dbadmin;

-- This is to mitigate CVE-2018-1058, see https://wiki.postgresql.org/wiki/A_Guide_to_CVE-2018-1058:_Protect_Your_Search_Path
-- only dbadmin will be allowed to create objects in public schema
REVOKE CREATE ON SCHEMA public FROM PUBLIC;
GRANT CREATE ON SCHEMA public TO dbadmin;

-- Preventing dbuser to query last executed statements in database -> this helps to secure the appliaciont little bit more
REVOKE EXECUTE ON FUNCTION pg_stat_get_activity(int) FROM dbuser;
REVOKE EXECUTE ON FUNCTION pg_stat_get_activity(int) FROM public;
GRANT EXECUTE ON FUNCTION pg_stat_get_activity(int) TO dbadmin;
REVOKE EXECUTE ON FUNCTION pg_stat_get_backend_activity(INTEGER) FROM dbuser;
REVOKE EXECUTE ON FUNCTION pg_stat_get_backend_activity(INTEGER) FROM public;
GRANT EXECUTE ON FUNCTION pg_catalog.pg_stat_get_backend_activity(INTEGER) TO dbadmin;

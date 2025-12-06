-- Specify default grants for dbuser so we don't have to specify it for each new table again and again
-- Note that we assume that role dbadmin will be creating all the tables (will be running flyway scripts)
ALTER DEFAULT PRIVILEGES FOR ROLE dbadmin REVOKE ALL ON FUNCTIONS FROM public;
ALTER DEFAULT PRIVILEGES FOR ROLE dbadmin IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO dbuser;
ALTER DEFAULT PRIVILEGES FOR ROLE dbadmin IN SCHEMA public GRANT EXECUTE ON FUNCTIONS TO dbuser;
ALTER DEFAULT PRIVILEGES FOR ROLE dbadmin IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO dbuser;
ALTER DEFAULT PRIVILEGES FOR ROLE dbadmin IN SCHEMA public GRANT USAGE ON TYPES TO dbuser;



-- View helpful when you suspect that there is hanging client, which holds database locks and which prevents
-- other clients to get access/locks on the database. This might happen if client starts transaction but
-- then dies without closing the connection. More info on this view and how to kill hanging processes here:
-- http://ghostwritten-insomnia.blogspot.cz/2013/04/show-blocking-postgres-processes-and.html
CREATE VIEW pg_blocking_processes AS
SELECT
    kl.pid as blocking_pid,
    ka.usename as blocking_user,
    ka.query as blocking_query,
    bl.pid as blocked_pid,
    a.usename as blocked_user,
    a.query as blocked_query,
    to_char(age(now(), a.query_start),'HH24h:MIm:SSs') as age
FROM pg_catalog.pg_locks bl
         JOIN pg_catalog.pg_stat_activity a
              ON bl.pid = a.pid
         JOIN pg_catalog.pg_locks kl
              ON bl.locktype = kl.locktype
                  and bl.database is not distinct from kl.database
                  and bl.relation is not distinct from kl.relation
                  and bl.page is not distinct from kl.page
                  and bl.tuple is not distinct from kl.tuple
                  and bl.virtualxid is not distinct from kl.virtualxid
                  and bl.transactionid is not distinct from kl.transactionid
                  and bl.classid is not distinct from kl.classid
                  and bl.objid is not distinct from kl.objid
                  and bl.objsubid is not distinct from kl.objsubid
                  and bl.pid <> kl.pid
         JOIN pg_catalog.pg_stat_activity ka
              ON kl.pid = ka.pid
WHERE kl.granted and not bl.granted
ORDER BY a.query_start;
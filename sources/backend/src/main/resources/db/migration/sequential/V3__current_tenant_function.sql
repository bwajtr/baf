
-------------------------------------------------------------------------------------------------------------
-- This function cannot be placed into "repeatable migration script" because it's used during tables creation
-------------------------------------------------------------------------------------------------------------

DROP FUNCTION IF EXISTS current_tenant();

-- Helper function to get tenant id of current session (transaction). Note that tenant should be set
-- to transaction using: "SET LOCAL session.tenant.id to xxx" where xxx is the tenant id
CREATE OR REPLACE FUNCTION current_tenant() RETURNS UUID
AS $$
BEGIN
  return session_param('session.tenant.id'::text)::UUID;
END;
$$
LANGUAGE plpgsql
STABLE PARALLEL SAFE;
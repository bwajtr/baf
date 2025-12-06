-------------------------------------------------------------------------------------------------------------
-- This function cannot be placed into "repeatable migration script" because it's used during tables creation
-------------------------------------------------------------------------------------------------------------

-- apply_tenant_policy will create row level security policy on given table on given column. It's primarily targeted
-- to enable row level security by tenant_id in easy way. It should help developers with row level security without
-- much thinking. This function will create the policy, enable row level security for a table, (optionally) create
-- an index on the policy column and analyzes the table so the planner is capable of using the index during selects.
--
-- This function should be used only when defining new table in migration scripts.
-- This function must be executed only by dbadmin user.

CREATE FUNCTION apply_tenant_policy(p_schema TEXT, p_table TEXT, p_tenant_column TEXT)
  RETURNS VOID
AS $$
BEGIN
  EXECUTE apply_tenant_policy(p_schema, p_table, p_tenant_column, TRUE);
END;
$$ LANGUAGE plpgsql;

REVOKE EXECUTE ON FUNCTION apply_tenant_policy(TEXT, TEXT, TEXT) FROM dbuser CASCADE;

-- overloaded version of the function which makes creation of the index optional (should be used on tables where
-- you are absolutely certain that the index won't be needed -> if in doubt then rather create the index)
CREATE FUNCTION apply_tenant_policy(p_schema TEXT, p_table TEXT, p_tenant_column TEXT, p_create_index BOOLEAN)
  RETURNS VOID
AS $$
DECLARE
  statement TEXT;
BEGIN
  statement := format('CREATE POLICY tenant_policy ON %I.%I USING (%I = current_tenant())', p_schema, p_table,
                      p_tenant_column);
  EXECUTE statement;

  statement := format('ALTER TABLE %I.%I ENABLE ROW LEVEL SECURITY', p_schema, p_table);
  EXECUTE statement;

  IF (p_create_index)
  THEN
    statement := format('CREATE INDEX IF NOT EXISTS %1$I_%2$I_%3$I ON %1$I.%2$I (%3$I)', p_schema, p_table,
                        p_tenant_column);
    EXECUTE statement;

    statement := format('ANALYZE %I.%I', p_schema, p_table);
    EXECUTE statement;
  END IF;

END;
$$ LANGUAGE plpgsql;

REVOKE EXECUTE ON FUNCTION apply_tenant_policy(TEXT, TEXT, TEXT, BOOLEAN) FROM dbuser CASCADE;





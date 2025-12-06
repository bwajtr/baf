
-------------------------------------------------------------------------------------------------------------
-- This function cannot be placed into "repeatable migration script" because it's used during tables creation
-------------------------------------------------------------------------------------------------------------

DROP FUNCTION IF EXISTS session_param( TEXT );

-- Helper function for working with session parameters. Throws exception if given parameter is not set in current session.
-- Why we need it: ordinary current_setting() function may return empty value if the parameter is not set and therefore
-- may pass incorrect value to SELECTS or UPDATES. This is testable by turning autocommit off, calling "SET LOCAL param TO value",
-- then ending the transaction (rollback) and then querying for this setting. In this situation current_setting(text) will return empty
-- string, but session_param(text) will throw exception as if the param was never set, which is what we want.
CREATE OR REPLACE FUNCTION session_param(name text) RETURNS text
AS $$
DECLARE
  setting_value text;
BEGIN
  ASSERT name IS NOT NULL, 'Input to session_param(text) function must not be null';

  setting_value := current_setting(name, true);
  IF setting_value IS NULL OR trim(setting_value) = '' THEN
    RAISE EXCEPTION 'Error: Session parameter ''%'' is not set. You are probably not in transaction. If absolutely necessary set it explicitly using: SET [LOCAL] "%" TO xxx', name, name;
  END IF;
  return setting_value;
END;
$$
LANGUAGE plpgsql
STABLE PARALLEL SAFE;

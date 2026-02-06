DROP FUNCTION IF EXISTS authenticate_user( TEXT, TEXT );

-- Function for verifying credentials of an user.
-- p_email and p_password are mandatory non-null fields
-- p_password is unencrypted
-- Returns:
--   "OK" if account with given email exists and password matches
--   "NOT_OK" otherwise
CREATE OR REPLACE FUNCTION authenticate_user(p_email TEXT, p_password TEXT)
  RETURNS TABLE(STATUS TEXT)
AS $$
DECLARE
  user_row user_account%ROWTYPE;

BEGIN
  -- preconditions
  ASSERT p_password IS NOT NULL, 'p_password must not be null';
  ASSERT p_email IS NOT NULL, 'p_email must not be null';

  SELECT *
  INTO user_row
  FROM user_account u
  WHERE lower(u.email) = lower(p_email);  -- we want case insensitivity on emails (jane@gmail.com is same account as JANE@gmail.com)

  IF (user_row.id IS NOT NULL)
  THEN
    -- verify password
    IF (user_row.password = ext_pgcrypto.crypt(p_password, user_row.password))
    THEN
      RETURN QUERY VALUES ('OK');
      RETURN;
    END IF;
  END IF;

  RETURN QUERY VALUES ('NOT_OK');
END;
-- Set a secure search_path: trusted schema(s), then 'pg_temp'. See SECURITY DEFINER documentation for why this is required.
$$
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp;

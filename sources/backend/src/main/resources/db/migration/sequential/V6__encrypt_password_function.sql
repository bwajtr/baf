CREATE OR REPLACE FUNCTION encrypt_password(password TEXT)
  RETURNS TEXT
AS $$ BEGIN
  -- Blowfish encryption with 13 iterations, each password with it's own salt
  RETURN ext_pgcrypto.crypt(password, ext_pgcrypto.gen_salt('bf', 13));
END;
$$ LANGUAGE plpgsql
STABLE PARALLEL SAFE;
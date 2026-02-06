-- Table holding users of the main application.
CREATE TABLE user_account (
  id UUID PRIMARY KEY DEFAULT uuidv7(), -- this primary key is required so it would be possible to change user email
  name TEXT NOT NULL,
  email TEXT NOT NULL UNIQUE, -- note that additional functional index is created for this column below
  password TEXT NOT NULL,
  email_verified BOOLEAN NOT NULL DEFAULT FALSE,
  email_verification_token UUID,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- this index will ensure that emails are unique in user_account table despite character case,
-- meaning that with this index you cannot have two records varying only in case (like "john.doe@gmail.com" and "John.Doe@gmail.com")
-- See: https://hashrocket.com/blog/posts/working-with-email-addresses-in-postgresql
create unique index user_account_unique_lower_email_idx on user_account (lower(email));

COMMENT ON COLUMN user_account.name IS 'Full name (first and last) of the user';
COMMENT ON COLUMN user_account.email IS 'Email of the user, must be unique because it''s used during the login process. Uniqueness is case-insensitive, you cannot store two user accounts like "john.doe@gmail.com" and "John.Doe@gmail.com"';
COMMENT ON COLUMN user_account.password IS 'Encrypted password';
COMMENT ON COLUMN user_account.email_verified IS 'True if ownership of the users email was verified';
COMMENT ON COLUMN user_account.email_verification_token IS 'Nullable UUID token used for the email ownership verification. Not null if verification is in progress.';
COMMENT ON COLUMN user_account.created_at IS 'Point in time when this record was created';

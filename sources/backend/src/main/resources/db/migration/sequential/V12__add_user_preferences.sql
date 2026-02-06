-- Add user preference columns for locale and timezone
ALTER TABLE user_account ADD COLUMN preferred_locale TEXT;
ALTER TABLE user_account ADD COLUMN preferred_timezone_id TEXT;

COMMENT ON COLUMN user_account.preferred_locale IS 'User''s preferred locale as language tag (e.g., en-US, cs-CZ)';
COMMENT ON COLUMN user_account.preferred_timezone_id IS 'User''s preferred timezone ID (e.g., Europe/Prague, America/New_York)';

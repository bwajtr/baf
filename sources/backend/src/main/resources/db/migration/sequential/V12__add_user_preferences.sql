-- Add user preference columns for locale and timezone
ALTER TABLE app_user ADD COLUMN preferred_locale TEXT;
ALTER TABLE app_user ADD COLUMN preferred_timezone_id TEXT;

COMMENT ON COLUMN app_user.preferred_locale IS 'User''s preferred locale as language tag (e.g., en-US, cs-CZ)';
COMMENT ON COLUMN app_user.preferred_timezone_id IS 'User''s preferred timezone ID (e.g., Europe/Prague, America/New_York)';


-- Table holding user login events - new record with a timestamp is added to this table when any user successfully logs into the application.
CREATE TABLE user_login_history (
  user_account_id UUID NOT NULL REFERENCES user_account (id) ON DELETE CASCADE,
  event_timestamp TIMESTAMPTZ NOT NULL,
  browser TEXT,
  device_type TEXT,
  operating_system TEXT,
  ip_address TEXT
);

COMMENT ON TABLE user_login_history IS 'Table holding user login events - new record with a timestamp is added to this table when any user successfully logs into the application.';
COMMENT ON COLUMN user_login_history.user_account_id IS 'Reference to user who logged into the application';
COMMENT ON COLUMN user_login_history.event_timestamp IS 'Timestamp of the login event';
COMMENT ON COLUMN user_login_history.browser IS 'Browser used for login (like Chrome, Firefox etc.) including version, if known. Can be null in which case the browser is unknown.';
COMMENT ON COLUMN user_login_history.device_type IS 'Device type used for login. Can be null in which case the device type couldn''t be determined. Example: Mobile, Tablet, Desktop';
COMMENT ON COLUMN user_login_history.operating_system IS 'Operating system used for login (like Windows 10, Mac OS X etc.) including version, if known. Can be null in which case the OS is unknown.';
COMMENT ON COLUMN user_login_history.ip_address IS 'IP Address of the device used during user login. Can be null if the IP Address couldn''t be determined';

create index user_login_history_by_user on user_login_history(user_account_id);

-- user should be allowed only to read from this table, not do any modifications to it
REVOKE INSERT, UPDATE, DELETE ON TABLE user_login_history FROM dbuser;

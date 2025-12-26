
-- Table holding user login events - new record with a timestamp is added to this table when any user successfully logs into the application.
CREATE TABLE user_login_log (
  app_user_id UUID NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
  event_timestamp TIMESTAMPTZ NOT NULL,
  browser TEXT,
  device_type TEXT,
  operating_system TEXT,
  ip_address TEXT
);

COMMENT ON TABLE user_login_log IS 'Table holding user login events - new record with a timestamp is added to this table when any user successfully logs into the application.';
COMMENT ON COLUMN user_login_log.app_user_id IS 'Reference to user who logged into the application';
COMMENT ON COLUMN user_login_log.event_timestamp IS 'Timestamp of the login event';
COMMENT ON COLUMN user_login_log.browser IS 'Browser used for login (like Chrome, Firefox etc.) including version, if known. Can be null in which case the browser is unknown.';
COMMENT ON COLUMN user_login_log.device_type IS 'Device type used for login. Can be null in which case the device type couldn''t be determined. Example: Mobile, Tablet, Desktop';
COMMENT ON COLUMN user_login_log.operating_system IS 'Operating system used for login (like Windows 10, Mac OS X etc.) including version, if known. Can be null in which case the OS is unknown.';
COMMENT ON COLUMN user_login_log.ip_address IS 'IP Address of the device used during user login. Can be null if the IP Address couldn''t be determined';

create index user_login_log_by_user on user_login_log(app_user_id);

-- app_user should be allowed only to read from this table, not do any modifications to it
REVOKE INSERT, UPDATE, DELETE ON TABLE user_login_log FROM dbuser;

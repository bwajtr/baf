-- Table holding API keys for tenant access. Each tenant has at most one API key.
CREATE TABLE tenant_api_key (
  id UUID PRIMARY KEY DEFAULT uuidv7(),
  api_key TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  tenant_id UUID NOT NULL UNIQUE REFERENCES tenant (id) ON DELETE CASCADE
);

COMMENT ON TABLE tenant_api_key IS 'API keys for programmatic access to tenant resources. Each tenant has at most one API key.';
COMMENT ON COLUMN tenant_api_key.api_key IS 'The API key string used for authentication';
COMMENT ON COLUMN tenant_api_key.created_at IS 'Timestamp when the API key was created or last reset';
COMMENT ON COLUMN tenant_api_key.tenant_id IS 'Tenant that owns this API key';

-- NOTE: No row level security on this table. It serves as an authentication source
-- and must be queryable without tenant context (similar to user_account).
GRANT SELECT ON tenant_api_key TO dbuser;

-- Table holding tenants of the application.
CREATE TABLE tenant (
  id UUID PRIMARY KEY DEFAULT uuidv7(),
  organization_name TEXT NOT NULL CHECK (trim(organization_name) != '' OR setup_required),
  organization_address TEXT,
  organization_country_code TEXT CHECK (char_length(organization_country_code) = 2 AND UPPER(organization_country_code) = organization_country_code),
  setup_required BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE tenant IS 'Represents a tenant (company) of the application. Almost everything in database is bound to a tenant. One tenant cannot see data of other tenants. It''s a way how to have multiple customers inside single database schema';
COMMENT ON COLUMN tenant.organization_name IS 'Name of the tenant visible to application users within application. Modifiable by tenant administrator user.';
COMMENT ON COLUMN tenant.organization_address IS 'Optional tenant''s company address (without country) - could be used for billing, but mainly for means of postal mail contact when needed';
COMMENT ON COLUMN tenant.organization_country_code IS 'Optional tenant (or tenant''s company) country of residence. Used for various legal concerns. Uses ISO 3166 2-letter country code';
COMMENT ON COLUMN tenant.setup_required IS 'If set to true then a setup screen is displayed to the Administrator when the application is run. Used for basic application setup and filling the mandatory fields.';

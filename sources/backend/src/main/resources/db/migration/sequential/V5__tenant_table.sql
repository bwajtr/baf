-- Table holding tenants of the application.
CREATE TABLE tenant (
  id UUID PRIMARY KEY DEFAULT uuidv7(),
  organization_name TEXT NOT NULL CHECK (trim(organization_name) != '' OR setup_required),
  organization_address TEXT,
  organization_country_code TEXT CHECK (char_length(organization_country_code) = 2 AND UPPER(organization_country_code) = organization_country_code),
  setup_required BOOLEAN NOT NULL DEFAULT TRUE
);

COMMENT ON TABLE tenant IS 'Represents a tenant (company) of the application. Almost everything in database is bound to a tenant. One tenant cannot see data of other tenants. It''s a way how to have multiple customers inside single database schema';
COMMENT ON COLUMN tenant.organization_name IS 'Name of the tenant visible to application users within application. Modifiable by tenant administrator user.';
COMMENT ON COLUMN tenant.organization_address IS 'Optional tenant''s company address (without country) - could be used for billing, but mainly for means of postal mail contact when needed';
COMMENT ON COLUMN tenant.organization_country_code IS 'Optional tenant (or tenant''s company) country of residence. Used for various legal concerns. Uses ISO 3166 2-letter country code';
COMMENT ON COLUMN tenant.setup_required IS 'If set to true then a setup screen is displayed to the Company Administrator when the application is run. Used for basic application setup and filling the mandatory fields.';

-- enable row level security (so the users see only data from it's tenant)
SELECT apply_tenant_policy('public', 'tenant', 'id', FALSE); -- we don't have to create index, because we already have one

-- define constant for developer tenant id - handy in other scripts (usable only by dbadmin user)
CREATE OR REPLACE FUNCTION developer_tenant_id()
  RETURNS UUID AS
$$SELECT '2dcab49d-8807-4888-bb69-2afd663e2743' :: UUID$$ LANGUAGE SQL IMMUTABLE;
-- access by user dbuser is revoked to prevent id information leak to normal users (adheres to principle that
-- dbuser must always see information related to current session tenant only))
REVOKE EXECUTE ON FUNCTION developer_tenant_id() FROM dbuser CASCADE;

-- define constants for JUnit tenant id - handy in other scripts (usable only by dbadmin user) when defining test data
CREATE OR REPLACE FUNCTION junit_tenant_id()
  RETURNS UUID AS
$$SELECT 'd6cfcd0a-9294-47f1-a6f2-29eed9994123' :: UUID$$ LANGUAGE SQL IMMUTABLE;
-- access by user dbuser is revoked to prevent id information leak to normal users (adheres to principle that
-- dbuser must always see information related to current session tenant only))
REVOKE ALL PRIVILEGES ON FUNCTION junit_tenant_id() FROM dbuser;

-- insert default values - tenants used for developer testing and JUnit execution
INSERT INTO tenant (id, organization_name, setup_required, organization_address, organization_country_code)
VALUES (developer_tenant_id(), 'Development Tenant', FALSE, 'Highlands 1/25, Prague','CZ'); -- tenant used by developers

INSERT INTO tenant (id, organization_name, setup_required, organization_address, organization_country_code)
VALUES (junit_tenant_id(), 'JUnit Tenant', FALSE, 'Highlands 1/25, Prague', 'CZ'); -- tenant used by JUnit tests

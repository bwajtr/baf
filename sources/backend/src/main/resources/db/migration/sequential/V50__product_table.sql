-- Product table definition
CREATE TABLE product (
  id UUID PRIMARY KEY DEFAULT uuidv7(),
  name TEXT NOT NULL,
  description TEXT,
  price NUMERIC(9,2) NOT NULL,
  tenant_id UUID NOT NULL DEFAULT current_tenant() REFERENCES tenant (id) ON DELETE CASCADE
);

-- Always comment on each column of the table (except the ID and service tail columns)
COMMENT ON TABLE product IS 'Products definitions';
COMMENT ON COLUMN product.name IS 'Name of the product';
COMMENT ON COLUMN product.tenant_id IS 'Tenant information';

-- REQUIRED ON ALL USER MODIFIABLE TABLES!: Enable row level security to support multi-tenancy -> user should see/update only
-- products within his/her own tenant
SELECT apply_tenant_policy('public', 'product', 'tenant_id');


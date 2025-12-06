-- Product table definition
CREATE TABLE product (
  id UUID PRIMARY KEY DEFAULT uuidv7(),
  name TEXT NOT NULL,
  description TEXT,
  price NUMERIC(9,2) NOT NULL,
  tenant_id UUID NOT NULL DEFAULT current_tenant() REFERENCES tenant (id) ON DELETE CASCADE
--   created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
--   created_by UUID DEFAULT users.current_context_user() REFERENCES users.app_user(id) ON DELETE SET NULL,
--   updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
--   updated_by UUID DEFAULT users.current_context_user() REFERENCES users.app_user(id) ON DELETE SET NULL
);

-- Always comment on each column of the table (except the ID and service tail columns)
COMMENT ON TABLE product IS 'Products definitions';
COMMENT ON COLUMN product.name IS 'Name of the product';
COMMENT ON COLUMN product.tenant_id IS 'Tenant information';
-- COMMENT ON COLUMN product.created_at IS 'Point in time when this record was created';
-- COMMENT ON COLUMN product.created_by IS 'ID of user which created this record. Can be null if user account was deleted.';
-- COMMENT ON COLUMN product.updated_at IS 'Point in time when this record was last updated';
-- COMMENT ON COLUMN product.updated_by IS 'ID of user who did the last update of this record. Can be null if user account was deleted.';


-- attach the update trigger, to update the updated_at and updated_by columns
-- select users.attach_update_service_tail_trigger('public', 'product');

-- INSERT INTO product (name, price, tenant_id, created_by, updated_by) VALUES ('Product 1', 12.45, developer_tenant_id(), '06a65138-8865-493f-b3aa-5f7a92490fa8'::UUID, '06a65138-8865-493f-b3aa-5f7a92490fa8'::UUID);
-- INSERT INTO product (name, description, price, tenant_id, created_by, updated_by) VALUES ('Product 2', 'Description of Product 2', 25.69, developer_tenant_id(), '06a65138-8865-493f-b3aa-5f7a92490fa8'::UUID, '06a65138-8865-493f-b3aa-5f7a92490fa8'::UUID);
-- INSERT INTO product (name, price, tenant_id, created_by, updated_by) VALUES ('Product 3', 99.9, junit_tenant_id(), 'fba2e753-230f-481f-93bb-52826b7e9b9c'::UUID, 'fba2e753-230f-481f-93bb-52826b7e9b9c'::UUID);
-- INSERT INTO product (name, description, price, tenant_id, created_by, updated_by) VALUES ('Product 4', 'Description, of Product 4', 49.9, junit_tenant_id(), 'fba2e753-230f-481f-93bb-52826b7e9b9c'::UUID, 'fba2e753-230f-481f-93bb-52826b7e9b9c'::UUID);

INSERT INTO product (name, price, tenant_id) VALUES ('Product 1', 12.45, developer_tenant_id());
INSERT INTO product (name, description, price, tenant_id) VALUES ('Product 2', 'Description of Product 2', 25.69, developer_tenant_id());
INSERT INTO product (name, price, tenant_id) VALUES ('Product 3', 99.9, junit_tenant_id());
INSERT INTO product (name, description, price, tenant_id) VALUES ('Product 4', 'Description, of Product 4', 49.9, junit_tenant_id());


-- Add new permissions related to this new table
-- INSERT INTO users.permission (code, name) VALUES ('PERM_PRODUCT_READ', 'Show products and their details') ON CONFLICT DO NOTHING;
-- INSERT INTO users.permission (code, name) VALUES ('PERM_PRODUCT_UPDATE', 'Add/Update products') ON CONFLICT DO NOTHING;
-- INSERT INTO users.permission (code, name) VALUES ('PERM_PRODUCT_DELETE', 'Remove products') ON CONFLICT DO NOTHING;

-- Assign new permissions to roles
-- INSERT INTO users.role_permission (role_id, permission_code)
-- VALUES
--   -- 'User' role (577137a8-1c2d-41df-b4f7-f7f35b614fae)
--   ('577137a8-1c2d-41df-b4f7-f7f35b614fae' :: UUID, 'PERM_PRODUCT_READ'),
--   ('577137a8-1c2d-41df-b4f7-f7f35b614fae' :: UUID, 'PERM_PRODUCT_UPDATE'),
--   ('577137a8-1c2d-41df-b4f7-f7f35b614fae' :: UUID, 'PERM_PRODUCT_DELETE')
-- ON CONFLICT DO NOTHING;

-- REQUIRED ON ALL USER MODIFIABLE TABLES!: Enable row level security to support multi-tenancy -> user should see/update only
-- products within his/her own tenant
SELECT apply_tenant_policy('public', 'product', 'tenant_id');

-- enable permission checks
-- SELECT users.apply_update_security_check('public', 'product', 'PERM_PRODUCT_UPDATE');
-- SELECT users.apply_insert_security_check('public', 'product', 'PERM_PRODUCT_UPDATE');
-- SELECT users.apply_delete_security_check('public', 'product', 'PERM_PRODUCT_DELETE');


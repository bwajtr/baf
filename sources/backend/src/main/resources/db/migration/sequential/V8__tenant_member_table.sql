-- Contains M:N relation between application users and their roles. Any role can be assigned to any user.
-- User therefore can have many roles and one role can be assigned to multiple users.
-- Having tenant_id in this table also makes it possible to support scenario, where single user operates within two tenants
-- and has different roles in each tenant.

CREATE TABLE tenant_member (
  user_id UUID NOT NULL REFERENCES user_account (id) ON DELETE CASCADE,
  role TEXT NOT NULL,
  tenant_id  UUID NOT NULL DEFAULT current_tenant() REFERENCES tenant (id) ON DELETE CASCADE,
  CONSTRAINT tenant_member_pk PRIMARY KEY (user_id, role, tenant_id)
);

COMMENT ON TABLE tenant_member IS 'Contains relation between application users and their roles in tenant. Any role can be assigned to any user. User therefore can have many roles and one role can be assigned to multiple users. Having tenant_id in this table also makes it possible to support scenario, where single user operates within two tenants and has different roles in each tenant.';
COMMENT ON COLUMN tenant_member.user_id IS 'User participating in the relation - referencing user_account.id';
COMMENT ON COLUMN tenant_member.role IS 'Role of this user within the tenant. Can be anything meaningful to the app.';
COMMENT ON COLUMN tenant_member.tenant_id IS 'Tenant id for which this role assignment is relevant';

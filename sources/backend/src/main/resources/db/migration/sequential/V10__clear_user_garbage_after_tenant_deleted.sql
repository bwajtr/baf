-- this trigger function simply removes all users from app_user table which are not assigned to any tenant
-- If last tenant of a user is deleted, the user is removed as well
CREATE OR REPLACE FUNCTION clear_user_garbage_after_tenant_deleted_trigger_func()
  RETURNS TRIGGER AS $$
BEGIN
  DELETE FROM app_user
  WHERE id IN (SELECT u.id
               FROM app_user u
                 LEFT JOIN app_user_role_tenant ut ON u.id = ut.user_id
               WHERE ut.tenant_id IS NULL);

  RETURN OLD;
END
$$
LANGUAGE plpgsql
SECURITY DEFINER; -- have to use this so we can "see" the dangling users (remember that in this trigger the users do not belong to any tenant anymore)

REVOKE EXECUTE ON FUNCTION clear_user_garbage_after_tenant_deleted_trigger_func() FROM dbuser;

--clear the user garbage (users not assigned to any tenants) after tenant is removed
CREATE TRIGGER clear_user_garbage_after_tenant_deleted
  AFTER DELETE
  ON tenant
  FOR EACH STATEMENT
EXECUTE PROCEDURE clear_user_garbage_after_tenant_deleted_trigger_func();


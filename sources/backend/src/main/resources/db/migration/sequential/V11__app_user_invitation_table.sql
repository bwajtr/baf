-- Table holding user invitations to tenants
CREATE TABLE app_user_invitation (
  id UUID PRIMARY KEY DEFAULT uuidv7(),
  email TEXT NOT NULL UNIQUE, -- note that additional functional index is created for this column below
  last_invitation_sent_time TIMESTAMPTZ DEFAULT NULL,
  tenant_id UUID NOT NULL REFERENCES tenant (id) ON DELETE CASCADE,
  invited_by UUID REFERENCES app_user(id) ON DELETE SET NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  email_verification_token UUID DEFAULT NULL
);

-- this index will ensure that emails are unique in app_user_invitation table despite character case,
-- meaning that with this index you cannot have two records varying only in case (like "john.doe@gmail.com" and "John.Doe@gmail.com")
-- See: https://hashrocket.com/blog/posts/working-with-email-addresses-in-postgresql
create unique index app_user_invitation_unique_idx on app_user_invitation (lower(email));


COMMENT ON TABLE app_user_invitation IS 'Table holding invitations to join tenant accounts (created typically by user administrators)';
COMMENT ON COLUMN app_user_invitation.email IS 'Email of the invited user';
COMMENT ON COLUMN app_user_invitation.last_invitation_sent_time IS 'Date and time of last "(re)send invitation email" operation, null if no invitation email was sent yet';
COMMENT ON COLUMN app_user_invitation.tenant_id IS 'Tenant into which the user is invited';
COMMENT ON COLUMN app_user_invitation.invited_by IS 'Existing user who created the invitation';
COMMENT ON COLUMN app_user_invitation.created_at IS 'Creation date and time of the invitation (first invitation email was sent on this time)';
COMMENT ON COLUMN app_user_invitation.email_verification_token IS 'Token which is used to verify the invited user email address. Null if no invitation email was sent yet';

-- REQUIRED ON ALL USER MODIFIABLE TABLES!: Enable row level security to support multi-tenancy security
SELECT apply_tenant_policy('public', 'app_user_invitation', 'tenant_id');

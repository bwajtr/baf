-- Table holding user invitations to tenants
CREATE TABLE member_invitation (
  id UUID PRIMARY KEY DEFAULT uuidv7(),
  email TEXT NOT NULL UNIQUE, -- note that additional functional index is created for this column below
  last_invitation_sent_time TIMESTAMPTZ DEFAULT NULL,
  tenant_id UUID NOT NULL REFERENCES tenant (id) ON DELETE CASCADE,
  invited_by UUID REFERENCES app_user(id) ON DELETE SET NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  role TEXT NOT NULL
);

-- this index will ensure that emails are unique in app_user_invitation table despite character case,
-- meaning that with this index you cannot have two records varying only in case (like "john.doe@gmail.com" and "John.Doe@gmail.com")
-- See: https://hashrocket.com/blog/posts/working-with-email-addresses-in-postgresql
create unique index member_invitation_unique_idx on member_invitation (lower(email));


COMMENT ON TABLE member_invitation IS 'Table holding invitations to join tenant accounts (created typically by user administrators)';
COMMENT ON COLUMN member_invitation.email IS 'Email of the invited user';
COMMENT ON COLUMN member_invitation.last_invitation_sent_time IS 'Date and time of last "(re)send invitation email" operation, null if no invitation email was sent yet';
COMMENT ON COLUMN member_invitation.tenant_id IS 'Tenant into which the user is invited';
COMMENT ON COLUMN member_invitation.invited_by IS 'Existing user who created the invitation';
COMMENT ON COLUMN member_invitation.created_at IS 'Creation date and time of the invitation (first invitation email was sent on this time)';
COMMENT ON COLUMN member_invitation.role IS 'Role of the user when the invitation is accepted';

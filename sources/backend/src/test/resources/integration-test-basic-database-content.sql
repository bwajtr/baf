--------------------------------------------------------------------------------------------------------------
-- This is a basic database data setup used as a foundation in the JUnit integration testing.
--------------------------------------------------------------------------------------------------------------
DO $$
    DECLARE
        junit_tenant_1_id           UUID := '019b25f2-3cc6-761c-9e6e-1c0d279bfd30'::UUID;
        junit_tenant_2_id           UUID := '019b25f2-6e55-7f32-bf82-9e2d116873ce'::UUID;
        encrypted_default_user_password TEXT := encrypt_password('test');
        joe_user_id                     UUID := '019b5aa6-97b6-7358-8ffe-bb68f70c8fc6'::UUID;
        jane_admin_id                   UUID := '019b5aa6-cd48-75f9-8b74-59878b0ea7d9'::UUID;
        junit_tenant_1_owner_id     UUID := '019b5aa6-eae4-76f0-9077-571f50df349b'::UUID;
        junit_tenant_2_owner_id     UUID := '019b5ab7-72c3-739d-b548-b13d1d59fe11'::UUID;
    BEGIN

        INSERT INTO tenant (id, organization_name, setup_required, organization_address, organization_country_code)
        VALUES (junit_tenant_1_id, 'Development Tenant 1', FALSE, 'Highlands 1/25, Prague', 'CZ'),
               (junit_tenant_2_id, 'Development Tenant 2', FALSE, 'Highlands 1/25, Prague', 'CZ');

        INSERT INTO app_user (id, name, email, password, email_verified, created_at, preferred_locale, preferred_timezone_id)
        VALUES (joe_user_id, 'Joe User', 'joe.user@acme.com',
                encrypted_default_user_password, TRUE, current_timestamp - interval '2 days', 'en-US', 'America/New_York'),
               (jane_admin_id, 'Jane Admin', 'jane.admin@acme.com',
                encrypted_default_user_password, TRUE, current_timestamp - interval '3 days', 'cs-CZ', 'Europe/Prague'),
               (junit_tenant_1_owner_id, 'Josh Owner', 'josh.owner@acme.com',
                encrypted_default_user_password, TRUE, current_timestamp - interval '4 days', NULL, NULL),
               (junit_tenant_2_owner_id, 'William Owner', 'william.owner@acme.com',
                encrypted_default_user_password, TRUE, current_timestamp - interval '5 days', NULL, NULL);

        INSERT INTO app_user_role_tenant (user_id, role, tenant_id)
        VALUES (joe_user_id, 'USER', junit_tenant_1_id),
               (jane_admin_id, 'ADMIN', junit_tenant_1_id),
               (jane_admin_id, 'USER', junit_tenant_2_id),
               (junit_tenant_1_owner_id, 'OWNER', junit_tenant_1_id),
               (junit_tenant_1_owner_id, 'BILLING_MANAGER', junit_tenant_1_id),
               (junit_tenant_1_owner_id, 'USER', junit_tenant_2_id),
               (junit_tenant_2_owner_id, 'OWNER', junit_tenant_2_id),
               (junit_tenant_2_owner_id, 'BILLING_MANAGER', junit_tenant_2_id);

        INSERT INTO user_login_log (app_user_id, event_timestamp, browser, device_type, operating_system, ip_address)
        VALUES (joe_user_id, current_timestamp - interval '1 day', 'Chrome 90', 'Desktop', 'Windows 10', '127.0.0.1'),
               (jane_admin_id, current_timestamp - interval '2 days', 'Firefox 88', 'Laptop', 'Ubuntu 20.04',
                '127.0.0.1'),
               (junit_tenant_1_owner_id, current_timestamp - interval '3 days', 'Safari 14', 'Tablet', 'iOS 14',
                '127.0.0.1');

        INSERT INTO member_invitation (id, email, tenant_id, invited_by, last_invitation_sent_time, role)
        VALUES ('019b5afd-3566-7f26-92b1-b63c7a6eae54'::UUID, 'invited1@acme.com', junit_tenant_1_id,
                junit_tenant_1_owner_id, current_timestamp, 'USER'),
               ('019b5afd-6774-7dd5-b2d1-b0f34c3e2b7c'::UUID, 'invited2@acme.com', junit_tenant_1_id,
                junit_tenant_1_owner_id, NULL, 'ADMIN'),
               (DEFAULT, 'invited.user1@acme.com', junit_tenant_2_id, junit_tenant_2_owner_id,
                current_timestamp, 'USER'),
               (DEFAULT, 'invited.user2@acme.com', junit_tenant_2_id, junit_tenant_2_owner_id, NULL, 'USER');


        INSERT INTO product (name, description, price, tenant_id)
        VALUES ('Product 1', null, 12.45, junit_tenant_1_id),
               ('Product 2', 'Description of Product 2', 25.69, junit_tenant_1_id),
               ('Product 3', null, 99.9, junit_tenant_2_id),
               ('Product 4', 'Description, of Product 4', 49.9, junit_tenant_2_id);

    END
$$;
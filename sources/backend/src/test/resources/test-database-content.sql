-- should be executed as dbadmin user

DO
$$
    DECLARE
        developer_tenant_1_id UUID := '2dcab49d-8807-4888-bb69-2afd663e2743';
        developer_tenant_2_id UUID := 'd6cfcd0a-9294-47f1-a6f2-29eed9994123';
    BEGIN
        INSERT INTO tenant (id, organization_name, setup_required, organization_address, organization_country_code)
        VALUES (developer_tenant_1_id, 'Development Tenant 1', FALSE, 'Highlands 1/25, Prague', 'CZ'),
               (developer_tenant_2_id, 'Development Tenant 2', FALSE, 'Highlands 1/25, Prague', 'CZ');


        INSERT INTO product (name, description, price, tenant_id)
        VALUES ('Product 1', null, 12.45, developer_tenant_1_id),
               ('Product 2', 'Description of Product 2', 25.69, developer_tenant_1_id),
               ('Product 3', null, 99.9, developer_tenant_2_id),
               ('Product 4', 'Description, of Product 4', 49.9, developer_tenant_2_id);

    END
$$;
ALTER TABLE assembly_component_workflows
    ADD CONSTRAINT ck_assembly_workflow_state_dates
    CHECK (
        (assembly_status = 'NOT_STARTED'
            AND assembly_start_date IS NULL
            AND assembly_complete_date IS NULL)
        OR (assembly_status = 'IN_PROGRESS'
            AND assembly_start_date IS NOT NULL
            AND assembly_complete_date IS NULL)
        OR (assembly_status = 'COMPLETED'
            AND assembly_start_date IS NOT NULL
            AND assembly_complete_date IS NOT NULL
            AND assembly_complete_date >= assembly_start_date)
    );

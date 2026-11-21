DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid = 'public.assembly_component_workflows'::regclass
          AND conname = 'ck_assembly_workflow_status'
    ) THEN
        ALTER TABLE assembly_component_workflows
            ADD CONSTRAINT ck_assembly_workflow_status
            CHECK (assembly_status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED'));
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid = 'public.assembly_component_workflows'::regclass
          AND conname = 'ck_assembly_workflow_dates'
    ) THEN
        ALTER TABLE assembly_component_workflows
            ADD CONSTRAINT ck_assembly_workflow_dates
            CHECK (assembly_complete_date IS NULL
                OR (assembly_start_date IS NOT NULL AND assembly_complete_date >= assembly_start_date));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_design_parts_main_status_mode
    ON design_register_parts (main_assembly_id, part_status, mode_of_purchase);

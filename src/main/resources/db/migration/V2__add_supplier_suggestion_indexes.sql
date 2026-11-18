CREATE INDEX IF NOT EXISTS idx_suppliers_name_prefix
    ON suppliers (lower(name) text_pattern_ops);

CREATE INDEX IF NOT EXISTS idx_suppliers_number_prefix
    ON suppliers (lower(supplier_number) text_pattern_ops);

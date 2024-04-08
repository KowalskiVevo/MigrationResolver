--${flyway:timestamp}
CREATE OR REPLACE VIEW version_view AS SELECT version();
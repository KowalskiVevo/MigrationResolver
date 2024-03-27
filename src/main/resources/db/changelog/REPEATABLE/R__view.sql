--liquibase formatted sql
--changeset kowalski:r_view runOnChange:true
CREATE VIEW version_view AS SELECT version();
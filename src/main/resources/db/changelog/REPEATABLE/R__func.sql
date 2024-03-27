--liquibase formatted sql
--changeset kowalski:r_func runOnChange:true
CREATE FUNCTION ${schema}.add(a integer, b integer) RETURNS integer
    LANGUAGE SQL
    IMMUTABLE
    RETURNS NULL ON NULL INPUT
RETURN a + b;
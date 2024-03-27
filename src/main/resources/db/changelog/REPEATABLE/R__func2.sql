--liquibase formatted sql
--changeset kowalski:r_func2  runOnChange:true
CREATE FUNCTION subtract(a integer, b integer) RETURNS integer
    LANGUAGE SQL
    IMMUTABLE
    RETURNS NULL ON NULL INPUT
RETURN a - b;
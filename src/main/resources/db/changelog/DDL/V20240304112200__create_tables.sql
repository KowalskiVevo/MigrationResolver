--liquibase formatted sql
--changeset kowalski:create_table
create table if not exists ${schema}.test_one
(
    id      bigserial not null primary key,
    value   varchar   null,
    content varchar   null
);

COMMENT ON TABLE ${schema}.test_one IS 'Test table';
COMMENT ON COLUMN ${schema}.test_one.id IS 'Identification';
COMMENT ON COLUMN ${schema}.test_one.content IS 'Content';

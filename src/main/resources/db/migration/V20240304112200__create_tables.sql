create table if not exists ${flyway:defaultSchema}.test_one
(
    id      bigserial not null primary key,
    value   varchar   null,
    content varchar   null
);

COMMENT ON TABLE ${flyway:defaultSchema}.test_one IS 'Test table';
COMMENT ON COLUMN ${flyway:defaultSchema}.test_one.id IS 'Identification';
COMMENT ON COLUMN ${flyway:defaultSchema}.test_one.content IS 'Content';

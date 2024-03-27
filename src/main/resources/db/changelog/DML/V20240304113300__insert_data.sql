--liquibase formatted sql
--changeset kowalski:insert_to_test_one
insert into ${schema}.test_one(value, content) values ('value', 'content');
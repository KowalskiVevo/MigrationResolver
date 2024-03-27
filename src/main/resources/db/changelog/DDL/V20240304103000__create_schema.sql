--liquibase formatted sql
--changeset kowalski:create_schema
CREATE SCHEMA IF NOT EXISTS ${schema};
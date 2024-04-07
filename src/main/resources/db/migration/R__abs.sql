CREATE OR REPLACE FUNCTION subtract(a integer, b integer) RETURNS integer AS
$$
BEGIN
    RETURN a - b;
END;
$$ LANGUAGE plpgsql;

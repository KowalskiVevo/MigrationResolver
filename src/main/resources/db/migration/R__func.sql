CREATE OR REPLACE FUNCTION sum_two_numbers(a integer, b integer) RETURNS integer AS
$$
BEGIN
    RETURN a + b;
END;
$$ LANGUAGE plpgsql;

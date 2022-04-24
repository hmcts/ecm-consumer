/* CREATE FUNCTION */
CREATE OR REPLACE FUNCTION fn_persistentQ_InsertFirstMultipleCountVal (p_multipleRef varchar(25)) RETURNS INTEGER AS $$

BEGIN

LOCK table multipleCounter;
INSERT into multipleCounter
SELECT p_multipleRef, 0
WHERE
    not exists (select 1 from multipleCounter where multipleref = p_multipleRef);
return 1;
END;
   $$ LANGUAGE plpgsql;

/* CREATE FUNCTION */

CREATE OR REPLACE FUNCTION fn_persistentQ_getNextMultipleCountVal (p_multipleRef varchar(25)) RETURNS Integer AS $$

-- =============================================
-- Author:		Mohammed Hafejee

-- TEST :		SELECT fn_persistentQ_getNextMultipleCountVal ('3265');
--
-- Create date: 10-JUN-2020
-- Description:	Function to return next incremental value for a multiple reference number passed in
-- VERSION	  :	10-JUN-2020	- 1.0  - Initial
-- =============================================


    DECLARE currentval integer;

BEGIN

    -- Acquire Lock on multipleCounter table

    SELECT counter INTO currentval FROM multipleCounter WHERE multipleRef = p_multipleRef FOR UPDATE ;

    CASE

    WHEN currentval IS NULL THEN
        currentval := 1;
    INSERT INTO multipleCounter(counter, multipleRef) VALUES (currentval, p_multipleRef);

    ELSE

    currentval = currentval + 1;
    UPDATE  multipleCounter SET counter = currentval WHERE multipleRef = p_multipleRef;

    END CASE;

    RETURN  currentval;

END;
   $$ LANGUAGE plpgsql;



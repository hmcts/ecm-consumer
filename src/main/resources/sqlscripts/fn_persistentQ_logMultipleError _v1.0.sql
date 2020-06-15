/* CREATE FUNCTION */

CREATE OR REPLACE FUNCTION fn_persistentQ_logMultipleError  (p_multipleRef varchar(25), p_ethosCaseRef varchar(25), p_description varchar(250)) RETURNS void AS $$

-- =============================================
-- Author:		Mohammed Hafejee

-- TEST :		SELECT fn_persistentQ_logMultipleError  ('3265','2400001/2020','Unprocessable State');
-- TEST :		SELECT fn_persistentQ_logMultipleError  ('3265','2400001/2020','Single Case Taken');
--				
-- Create date: 10-JUN-2020
-- Description:	Function to log error messages encountered during the operation of the ECM multiples persistent queue 
-- VERSION	  :	10-JUN-2020		1.0  - Initial
-- =============================================

    
BEGIN 
    
    INSERT INTO multipleErrors VALUES (p_multipleRef, p_ethosCaseRef, p_description);
    
END;
   $$ LANGUAGE plpgsql;



/* CREATE TABLES FOR PERSISTENT QUEUE */

-- =============================================
-- Author:		Mohammed Hafejee
--				
-- Create date: 10-JUN-2020
-- Description:	Script to create base table used by ECM multiples persistent queue for error logging
-- Called by  : fn_persistentQ_logMultipleError
-- VERSION	  :	10-JUN-2020		1.0  - Initial
--        	  :	19-APR-2021		1.1  - Added identity column primary key
-- =============================================

/***********   multipleErrors   ************/  

DROP TABLE IF EXISTS multipleErrors;
CREATE TABLE multipleErrors 
  (
  id           serial PRIMARY KEY,
  multipleRef  varchar(25),
  ethosCaseRef varchar(25),
  description varchar(250)
  );

CREATE INDEX IX_multipleErrors_multipleRef_ethosCaseRef ON multipleErrors(multipleRef,ethosCaseRef);


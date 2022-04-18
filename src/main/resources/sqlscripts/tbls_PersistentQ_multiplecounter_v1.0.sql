/* CREATE TABLES FOR PERSISTENT QUEUE */

-- =============================================
-- Author:		Mohammed Hafejee
--
-- Create date: 10-JUN-2020
-- Description:	Script to create base table used by ECM multiples persistent queue
-- Called by  : fn_persistentQ_getNextMultipleCountVal
-- VERSION	:	10-JUN-2020		1.0  - Initial
-- =============================================

/***********   multipleCounter   ************/

DROP TABLE IF EXISTS multipleCounter;
CREATE TABLE multipleCounter
  (
  multipleRef  varchar(25),
  counter integer DEFAULT 1
  );

ALTER TABLE multipleCounter
    ADD CONSTRAINT multipleCounter_pk
        PRIMARY KEY (multipleRef, counter);

CREATE INDEX IX_multipleCounter_multipleRef ON multipleCounter(multipleRef);


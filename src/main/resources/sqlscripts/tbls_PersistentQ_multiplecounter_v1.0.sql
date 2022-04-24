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
-- clean up - removing duplicates before adding constraint
DELETE   FROM "multiplecounter" T1
USING       "multiplecounter" T2
WHERE  T1.ctid    < T2.ctid       -- delete the "older" ones
  AND  T1.multipleRef    = T2.multipleRef
  AND  T1.counter = T2.counter;

ALTER TABLE multipleCounter
    ADD CONSTRAINT multipleCounter_pk
        PRIMARY KEY (multipleRef);

CREATE INDEX IX_multipleCounter_multipleRef ON multipleCounter(multipleRef);


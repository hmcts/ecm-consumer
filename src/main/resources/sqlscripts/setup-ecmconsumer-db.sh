#!/usr/bin/env bash
# Creates ecmconsumer db for local dev, and its tables and functions that are required by ecm-consumer service

echo "Creating ecmconsumer database"
psql postgresql://localhost:5050 -v ON_ERROR_STOP=1 -U postgres <<-EOSQL
  CREATE USER ecmconsumer WITH PASSWORD 'ecmconsumer';

  CREATE DATABASE ecmconsumer
    WITH OWNER = ecmconsumer
    ENCODING = 'UTF-8'
    CONNECTION LIMIT = -1;
EOSQL

set -e

echo "Running tbls_PersistentQ_multiplecounter_v1.0.sql"
psql postgresql://localhost:5050/ecmconsumer -U ecmconsumer -f ./tbls_PersistentQ_multiplecounter_v1.0.sql

echo "Running tbls_PersistentQ_multipleErrors_v1.1.sql"
psql postgresql://localhost:5050/ecmconsumer -U ecmconsumer -f ./tbls_PersistentQ_multipleErrors_v1.1.sql

echo "Running fn_persistentQ_getNextMultipleCountVal_v1.0.sql"
psql postgresql://localhost:5050/ecmconsumer -U ecmconsumer -f ./fn_persistentQ_getNextMultipleCountVal_v1.0.sql

echo "Running fn_persistentQ_logMultipleError_v1.3.sql"
psql postgresql://localhost:5050/ecmconsumer -U ecmconsumer -f ./fn_persistentQ_logMultipleError_v1.3.sql

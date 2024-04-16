#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE fhir;
    GRANT ALL PRIVILEGES ON DATABASE fhir TO liquibase_user;
    CREATE DATABASE bpe;
    GRANT ALL PRIVILEGES ON DATABASE bpe TO liquibase_user;
EOSQL
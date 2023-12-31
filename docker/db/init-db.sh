#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE dic1_fhir;
    GRANT ALL PRIVILEGES ON DATABASE dic1_fhir TO liquibase_user;
    CREATE DATABASE dic1_bpe;
    GRANT ALL PRIVILEGES ON DATABASE dic1_bpe TO liquibase_user;
    CREATE DATABASE dic2_fhir;
    GRANT ALL PRIVILEGES ON DATABASE dic2_fhir TO liquibase_user;
    CREATE DATABASE dic2_bpe;
    GRANT ALL PRIVILEGES ON DATABASE dic2_bpe TO liquibase_user;
    CREATE DATABASE dms_fhir;
    GRANT ALL PRIVILEGES ON DATABASE dms_fhir TO liquibase_user;
    CREATE DATABASE dms_bpe;
    GRANT ALL PRIVILEGES ON DATABASE dms_bpe TO liquibase_user;
    CREATE DATABASE hrp_fhir;
    GRANT ALL PRIVILEGES ON DATABASE hrp_fhir TO liquibase_user;
    CREATE DATABASE hrp_bpe;
    GRANT ALL PRIVILEGES ON DATABASE hrp_bpe TO liquibase_user;
EOSQL
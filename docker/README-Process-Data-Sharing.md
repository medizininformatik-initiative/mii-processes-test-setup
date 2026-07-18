# Process Data-Sharing
## Preparations

Generate user specific dev setup files:

```sh
mvn dsf:generate-dev-setup-cert-files
```

Add entries to your hosts file:

```
127.0.0.1	dic1
127.0.0.1	dic2
127.0.0.1	dms
127.0.0.1	hrp
```

In order for following commands to work, the process plugin folder `mii-process-data-sharing` must be located next to the mii dev setup folder `mii-processes-dev-setup`.

## DIC 1
### DIC 1: FHIR Store

Start DIC1 HAPI FHIR store or DIC1 BLAZE FHIR store:

```sh
docker-compose up -d dic1-fhir-store-hapi && docker-compose logs -f dic1-fhir-store-hapi
docker-compose up -d dic1-fhir-store-blaze && docker-compose logs -f dic1-fhir-store-blaze
```

Access to DIC1 FHIR store at http://localhost:8080/fhir.

Add test-data using a transaction Bundle:

```sh
curl -H "Accept: application/fhir+xml" -H "Content-Type: application/fhir+xml" \
-d @../mii-process-data-transfer/src/test/resources/fhir/Bundle/DicFhirStore_Demo_CSV.xml \
http://localhost:8080/fhir
```

### DIC 1: DSF

Start DIC1 DSF FHIR server and wait till started:

```sh
docker-compose up -d dic1-fhir && docker-compose logs -f dic1-fhir
```

Start DIC1 DSF BPE server:

```sh
docker-compose up -d dic1-bpe && docker-compose logs -f dic1-fhir dic1-bpe
````

## DIC 2
### DIC 2: FHIR Store

Start DIC2 HAPI FHIR store or DIC2 BLAZE FHIR store:

```sh
docker-compose up -d dic2-fhir-store-hapi && docker-compose logs -f dic2-fhir-store-hapi
docker-compose up -d dic2-fhir-store-blaze && docker-compose logs -f dic2-fhir-store-blaze
```

Access to DIC2 FHIR store at http://localhost:8081/fhir.

Add test-data using a transaction Bundle:

```sh
ACCESS_TOKEN=$(curl -k -s -d 'grant_type=client_credentials' -u 'account:e11a3a8e-6e24-4f9d-b914-da7619e8b31f' \
https://localhost:8443/realms/blaze/protocol/openid-connect/token | jq -r .access_token)

curl -H "Accept: application/fhir+xml" -H "Content-Type: application/fhir+xml" --oauth2-bearer "${ACCESS_TOKEN}" \
-d @../mii-process-data-sharing/src/test/resources/fhir/Bundle/Dic2FhirStore_Demo_Bundle.xml \
http://localhost:8081/fhir
```

### DIC 2: DSF

Start DIC2 DSF FHIR server and wait till started:

```sh
docker-compose up -d dic2-fhir && docker-compose logs -f dic2-fhir
```

Start DIC2 DSF BPE server:

```sh
docker-compose up -d dic2-bpe && docker-compose logs -f dic2-fhir dic2-bpe
````

## DMS
### DMS: FHIR Store

Start DMS HAPI FHIR store or DMS BLAZE FHIR store:

```sh
docker-compose up -d dms-fhir-store-hapi && docker-compose logs -f dms-fhir-store-hapi
docker-compose up -d dms-fhir-store-blaze && docker-compose logs -f dms-fhir-store-blaze
```

Access to DMS FHIR store at http://localhost:8082/fhir.

### DMS: DSF

Start DMS DSF FHIR server and wait till started:

```sh
docker-compose up -d dms-fhir && docker-compose logs -f dms-fhir
```

Start DMS DSF BPE server:

```sh
docker-compose up -d dms-bpe && docker-compose logs -f dms-fhir dms-bpe
````

## HRP
### HRP: DSF

Start HRP DSF FHIR server and wait till started:

```sh
docker-compose up -d hrp-fhir && docker-compose logs -f hrp-fhir
```

Start HRP DSF BPE server:

```sh
docker-compose up -d hrp-bpe && docker-compose logs -f hrp-fhir hrp-bpe
````

## Process Execution

Open [https://hrp/fhir/Task?_sort=_profile,identifier&status=draft&_profile=http://medizininformatik-initiative.de/fhir/StructureDefinition/task-coordinate-data-sharing|2.0](https://hrp/fhir/Task?_sort=_profile,identifier&status=draft&_profile=http://medizininformatik-initiative.de/fhir/StructureDefinition/task-coordinate-data-sharing|2.0), select the process to be executed, add the inputs, and start the process.

Execute DIC1 user-task to release data-set for DMS based on the URL in the log output of dic1-bpe.

Execute DIC2 user-task to release data-set for DMS based on the URL in the log output of dic2-bpe.

Check transferred data to DMS (2 DocumentReferences expected):

```sh
curl -H "Accept: application/fhir+xml" \
http://localhost:8082/fhir/DocumentReference?identifier=Test_PROJECT_Bundle
```

Execute HRP user-task to release consolidation of data-set for DSM based on the URL in the log output of hrp-bpe.

Execute DMS user-task to release merged data-set for HRP based on the URL in the log output of dms-bpe.

Check if the Task starting the coordination process at the HRP contains a Task.output with code `data-set-location` containing the URL inserted as part of the user-task at the DMS.

## End

Stop everything:

```sh
cd docker
docker-compose down -v
```
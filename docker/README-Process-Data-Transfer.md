# Process Data-Transfer## Preparations

Generate user specific dev setup files:

```sh
mvn dsf:generate-dev-setup-cert-files
```

Add entries to your hosts file:

```
127.0.0.1	dic1
127.0.0.1	dms
```

In order for following commands to work, the process plugin folder `mii-process-data-transfer` must be located next to the mii dev setup folder `mii-processes-dev-setup`.

Generate user specific dev setup files by executing.

```sh
mvn dsf:generate-dev-setup-cert-files
```

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

To test large data-sets, first create a Binary and then the corresponding DocumentReference resource:

```sh
curl -v -X POST -H "Accept: application/fhir+xml" -H "Prefer: return=OperationOutcome" -H "Content-Type: application/x-ndjson" \
-T ../mii-process-data-transfer/src/test/resources/fhir/Bundle/result.ndjson \
http://localhost:8080/fhir/Binary
```

Replace the Binary resource-id in the file `../mii-process-data-transfer/src/test/resources/fhir/Bundle/DicFhirStore_Demo_LargeContent.xml` and execute it against the DIC1 FHIR store

### DIC 1: DSF

Start DIC1 DSF FHIR server and wait till started:

```sh
docker-compose up -d dic1-fhir && docker-compose logs -f dic1-fhir
```

Start DIC1 DSF BPE server:

```sh
docker-compose up -d dic1-bpe && docker-compose logs -f dic1-fhir dic1-bpe
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

## Process Execution

Open [https://dic1/fhir/Task?_sort=_profile,identifier&status=draft&_profile=http://medizininformatik-initiative.de/fhir/StructureDefinition/task-data-send-start|2.0](https://dic1/fhir/Task?_sort=_profile,identifier&status=draft&_profile=http://medizininformatik-initiative.de/fhir/StructureDefinition/task-data-send-start|2.0), select the process to be executed, add inputs if needed, and start the process.

Check transferred data to DMS:

```sh
curl -H "Accept: application/fhir+xml" \
http://localhost:8082/fhir/DocumentReference?identifier=Test_PROJECT_Bundle
```

## End

Stop everything:

```sh
cd docker
docker-compose down -v
```
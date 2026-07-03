# Process Data-Transfer

Generate user specific dev setup files by executing.

```sh
mvn dsf:generate-dev-setup-cert-files
```

Add entries to your hosts file

```
127.0.0.1	dic1
127.0.0.1	dms
```

*A total of five console windows are required. Start docker-compose commands for consoles 1 to 4 from
sub-folder:* `mii-processes-dev-setup/docker`

Console 1: Start DIC1 HAPI FHIR store or DIC1 BLAZE FHIR store

```sh
docker-compose up -d dic1-fhir-store-hapi && docker-compose logs -f dic1-fhir-store-hapi
docker-compose up -d dic1-fhir-store-blaze && docker-compose logs -f dic1-fhir-store-blaze
```

Access at http://localhost:8080/fhir/

Console 2: Start DIC1 DSF FHIR server and wait till started

```sh
docker-compose up -d dic1-fhir && docker-compose logs -f dic1-fhir
```

Console 2: Disconnect from log output (Ctrl-C) if server started
Console 2: Start DIC1 DSF BPE server

```sh
docker-compose up -d dic1-bpe && docker-compose logs -f dic1-fhir dic1-bpe
```

Console 3: Start DMS HAPI FHIR store or DMS BLAZE FHIR store

```sh
docker-compose up -d dms-fhir-store-hapi && docker-compose logs -f dms-fhir-store-hapi
docker-compose up -d dms-fhir-store-blaze && docker-compose logs -f dms-fhir-store-blaze
```

Access at http://localhost:8082/fhir/


Console 4: Start DMS DSF FHIR server and wait till started

```sh
docker-compose up -d dms-fhir && docker-compose logs -f dms-fhir
```

Console 4: Disconnect from log output (Ctrl-C) if server started 
Console 4: Start DMS DSF BPE server

```sh
docker-compose up -d dms-bpe && docker-compose logs -f dms-fhir dms-bpe
````

<!-- EXECUTE PROCESS -->

*Start curl commands in console 5 from root-folder:* `mii-processes-dev-setup`. In order for the commands
to be executed, the process plugin folder `mii-process-data-transfer` must be located next to the test setup folder
`mii-processes-dev-setup`.

Console 5: Execute Demo Transaction-Bundle for DIC1 FHIR store

```sh
curl -H "Accept: application/xml+fhir" -H "Content-Type: application/fhir+xml" \
-d @../mii-process-data-transfer/src/test/resources/fhir/Bundle/DicFhirStore_Demo_CSV.xml \
http://localhost:8080/fhir
```

To test large data-sets, use the following commands to first create a Binary and then the corresponding
DocumentReference resource on the DIC1 FHIR store

```sh
curl -v -X POST -H "Accept: application/fhir+xml" -H "Prefer: return=OperationOutcome" -H "Content-Type: application/x-ndjson" \
-T ../mii-process-data-transfer/src/test/resources/fhir/Bundle/result.ndjson \
http://localhost:8080/fhir/Binary
```

Replace the Binary resource id in the file
`../mii-process-data-transfer/src/test/resources/fhir/Bundle/DicFhirStore_Demo_LargeContent.xml` and execute it
against the DIC1 FHIR store

```sh
curl -H "Accept: application/xml+fhir" -H "Content-Type: application/fhir+xml" \
-d @../mii-process-data-transfer/src/test/resources/fhir/Bundle/DicFhirStore_Demo_LargeContent.xml \
http://localhost:8080/fhir
```

Console 5: Start Data Send Process at DIC1 using the following command

*Unfortunately this command does not work on Windows. An alternative for starting the process is using WSL or the
example starter class with name* `DataSendExampleStarter` *in* `../mii-process-data-transfer/src/test/java/../bpe/start`

```sh
curl -H "Accept: application/xml+fhir" -H "Content-Type: application/fhir+xml" \
-d @../mii-process-data-transfer/src/test/resources/fhir/Task/TaskDataSendStart_Demo_CSV.xml \
--ssl-no-revoke --cacert cert/DSF_DEV_Root_CA.crt \
--cert cert/Webbrowser_Test_User.crt \
--key cert/Webbrowser_Test_User.key \
--pass password \
https://dic1/fhir/Task
```

Console 5: Check data-transferred to DMS (1 DocumentReferences expected)

```sh
curl http://localhost:8082/fhir/DocumentReference
```

Console 5: Stop everything

```sh
cd docker
docker-compose down -v
```
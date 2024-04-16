# Process Data-Sharing

Follow the instructions in the **General Setup** section in [README.md](./README.md) before continuing.

Build the project from the root directory of this repository by executing the following command.

```sh
docker compose -f docker/docker-compose.yml up -d init
```

Add entries to your hosts file

```
127.0.0.1	dic1
127.0.0.1	dic2
127.0.0.1	dms
127.0.0.1	hrp
```

*A total of eight console windows are required. Start docker-compose commands for consoles 1 to 7 from
sub-folder:* `mii-processes-test-setup/docker`

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

Console 3: Start DIC2 HAPI FHIR store or DIC2 BLAZE FHIR store

```sh
docker-compose up -d dic2-fhir-store-hapi && docker-compose logs -f dic2-fhir-store-hapi
docker-compose up -d dic2-fhir-store-blaze && docker-compose logs -f dic2-fhir-store-blaze
```

Access at http://localhost:8081/fhir/

Console 4: Start DIC2 DSF FHIR server and wait till started

```sh
docker-compose up -d dic2-fhir && docker-compose logs -f dic2-fhir
```

Console 4: Disconnect from log output (Ctrl-C) if server started
Console 4: Start DIC2 DSF BPE server

```sh
docker-compose up -d dic2-bpe && docker-compose logs -f dic2-fhir dic2-bpe
```

Console 5: Start DMS HAPI FHIR store or DMS BLAZE FHIR store

```sh
docker-compose up -d dms-fhir-store-hapi && docker-compose logs -f dms-fhir-store-hapi
docker-compose up -d dms-fhir-store-blaze && docker-compose logs -f dms-fhir-store-blaze
```

Access at http://localhost:8082/fhir/

Console 6: Start DMS DSF FHIR server and wait till started

```sh
docker-compose up -d dms-fhir && docker-compose logs -f dms-fhir
```

Console 6: Disconnect from log output (Ctrl-C) if server started
Console 6: Start DMS DSF BPE server

```sh
docker-compose up -d dms-bpe && docker-compose logs -f dms-fhir dms-bpe
```

Console 7: Start HRP DSF FHIR server and wait till started

```sh
docker-compose up -d hrp-fhir && docker-compose logs -f hrp-fhir
```

Console 7: Disconnect from log output (Ctrl-C) if server started
Console 7: Start HRP DSF BPE server

```sh
docker-compose up -d hrp-bpe && docker-compose logs -f hrp-fhir hrp-bpe
````

<!-- EXECUTE PROCESS -->

*Start curl commands in console 8 from root-folder:* `mii-processes-test-setup`. In order for the commands
to be executed, the process plugin folder `mii-process-data-sharing` must be located next to the test setup folder
`mii-processes-test-setup`.

Console 8: Execute Demo Transaction-Bundle for DIC1 HAPI FHIR server

```sh
curl -H "Accept: application/xml+fhir" -H "Content-Type: application/fhir+xml" \
-d @../mii-process-data-sharing/src/test/resources/fhir/Bundle/Dic1FhirStore_Demo_Bundle.xml \
http://localhost:8080/fhir
```

Console 8: Execute Demo Transaction-Bundle for DIC2 HAPI FHIR server

```sh
curl -H "Accept: application/xml+fhir" -H "Content-Type: application/fhir+xml" \
-d @../mii-process-data-sharing/src/test/resources/fhir/Bundle/Dic2FhirStore_Demo_Bundle.xml \
http://localhost:8081/fhir
```

Console 8: Start Data Send Process at HRP using the following command

*Unfortunately this command does not work on Windows. An alternative for starting the process is using WSL or the
example starter class with name* `CoordinateDataSharingExampleStarter` *in* `../mii-process-data-sharing/src/test/java/../bpe/start`

```sh
curl -H "Accept: application/xml+fhir" -H "Content-Type: application/fhir+xml" \
-d @../mii-process-data-sharing/src/test/resources/fhir/Task/TaskCoordinateDataSharing_Demo_Bundle.xml \
--ssl-no-revoke --cacert cert/ca/testca_certificate.pem \
--cert cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.pem \
--key cert/Webbrowser_Test_User/Webbrowser_Test_User_private-key.pem \
--pass password \
https://hrp/fhir/Task
```

Console 2: Execute DIC1 user-task to release data-set for DMS based on the URL in the log output from console 2
Console 4: Execute DIC2 user-task to release data-set for DMS based on the URL in the log output from console 4

Console 8: Check data-transferred to DMS (2 DocumentReferences expected)

```sh
curl http://localhost:8082/fhir/DocumentReference
```

Console 6: Execute DMS user-task to release merged data-set for HRP based on the URL in the log output from console 6

console 8 : Check if the Task starting the coordination process at the HRP contains a Task.output with
            code `data-set-location` containing the URL inserted as part of the user-task at the DMS
```sh
curl -H "Accept: application/xml+fhir" \
--ssl-no-revoke --cacert cert/ca/testca_certificate.pem \
--cert cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.pem \
--key cert/Webbrowser_Test_User/Webbrowser_Test_User_private-key.pem \
--pass password \
https://hrp/fhir/Task?_sort=-_lastUpdated
```

Console 8: Stop everything

```sh
cd docker
docker-compose down -v
```
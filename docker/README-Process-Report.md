# Process Report

Build the project from the root directory of this repository by executing the following command.

```sh
mvn clean package
```

Add entries to your hosts file

```
127.0.0.1	dic1
127.0.0.1	hrp
```

*A total of five console windows are required. Start docker-compose commands for consoles 1 to 3 from
sub-folder:* `mii-processes-test-setup/docker`

Console 1: Start DIC1 HAPI FHIR store or DIC1 BLAZE FHIR store

```sh
docker-compose up -d dic1-fhir-store-hapi && docker-compose logs -f dic1-fhir-store-hapi
docker-compose up -d dic1-fhir-store-blaze && docker-compose logs -f dic1-fhir-store-blaze
```

Access to DIC1 FHIR store at http://localhost:8080/fhir

Console 2: Start DIC1 DSF FHIR server and wait till started

```sh
docker-compose up -d dic1-fhir && docker-compose logs -f dic1-fhir
```

Console 2: Disconnect from log output (Ctrl-C) if server started
Console 2: Start DIC1 DSF BPE server

```sh
docker-compose up -d dic1-bpe && docker-compose logs -f dic1-fhir dic1-bpe
````

Console 3: Start HRP DSF FHIR server and wait till started

```sh
docker-compose up -d hrp-fhir && docker-compose logs -f hrp-fhir
```

Console 3: Disconnect from log output (Ctrl-C) if server started 
Console 3: Start HRP DSF BPE server

```sh
docker-compose up -d hrp-bpe && docker-compose logs -f hrp-fhir hrp-bpe
````

<!-- TESTDATA -->

*Start commands in console 4 from sub-folder `mii-processes-test-setup/data`*
*Unfortunately the testdata does not work with a HAPI FHIR server*

Console 4: Download and insert data into DIC1 FHIR store

```sh
./download.sh
./insert.sh http://localhost:8080/fhir
```

<!-- EXECUTE PROCESS -->

*Start curl commands in console 5 from root-folder:* `mii-processes-test-setup`. In order for the commands
to be executed, the process plugin folder `mii-process-report` must be located next to the test setup folder
`mii-processes-test-setup`.

Console 5: Add the search Bundle to HRP DSF FHIR server

```sh
curl -H "Accept: application/xml+fhir" -H "Content-Type: application/fhir+xml" \
-d @../mii-process-report/src/test/resources/fhir/Bundle/search-bundle.xml \
--ssl-no-revoke --cacert cert/ca/testca_certificate.pem \
--cert cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.pem \
--key cert/Webbrowser_Test_User/Webbrowser_Test_User_private-key.pem \
--pass password \
https://hrp/fhir/Bundle
```

Console 5: Start Report Send Process at DIC1 using the following command

*Unfortunately this command does not work on Windows. An alternative for starting the process is using WSL or the
example starter class with name* `ReportSendExampleStarter` *in* `../mii-process-report/src/test/java/../bpe/start`

```sh
curl -H "Accept: application/xml+fhir" -H "Content-Type: application/fhir+xml" \
-d @../mii-process-report/src/test/resources/fhir/Task/TaskReportSendStart_Demo.xml \
--ssl-no-revoke --cacert cert/ca/testca_certificate.pem \
--cert cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.pem \
--key cert/Webbrowser_Test_User/Webbrowser_Test_User_private-key.pem \
--pass password \
https://dic1/fhir/Task
```

Console 5: Check data-transferred to HRP

```sh
curl -H "Accept: application/xml+fhir" \
--ssl-no-revoke --cacert cert/ca/testca_certificate.pem \
--cert cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.pem \
--key cert/Webbrowser_Test_User/Webbrowser_Test_User_private-key.pem \
--pass password \
https://hrp/fhir/Bundle?identifier=http://dsf.dev/sid/organization-identifier|Test_DIC1
```

Console 5: Stop everything

```sh
cd docker
docker-compose down -v
```
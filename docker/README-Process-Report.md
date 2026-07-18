# Process Report
## Preparations

Generate user specific dev setup files:

```sh
mvn dsf:generate-dev-setup-cert-files
```

Add entries to your hosts file:

```
127.0.0.1	dic1
127.0.0.1	hrp
```

In order for following commands to work, the process plugin folder `mii-process-report` must be located next to the mii dev setup folder `mii-processes-dev-setup`.

## DIC 1
### DIC 1: FHIR Store

Start DIC1 HAPI FHIR store or DIC1 BLAZE FHIR store:

```sh
docker-compose up -d dic1-fhir-store-hapi && docker-compose logs -f dic1-fhir-store-hapi
docker-compose up -d dic1-fhir-store-blaze && docker-compose logs -f dic1-fhir-store-blaze
```

Access to DIC1 FHIR store at http://localhost:8080/fhir.

Download and insert data into DIC1 FHIR store executing commands from sub-folder `mii-processes-dev-setup/data`.
(*Unfortunately the testdata does not work with a HAPI FHIR server*).

```sh
./download.sh
./insert.sh http://localhost:8080/fhir
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

Add the search Bundle to HRP DSF FHIR server:

```sh
curl -H "Accept: application/fhir+xml" -H "Content-Type: application/fhir+xml" \
-d @../mii-process-report/src/test/resources/fhir/Bundle/search-bundle-v2.0.xml \
--ssl-no-revoke --cacert cert/DSF_DEV_Root_CA.crt \
--cert cert/Webbrowser_Test_User.crt \
--key cert/Webbrowser_Test_User.key \
--pass password \
https://hrp/fhir/Bundle
```

## Process Execution

Open [https://dic1/fhir/Task?_sort=_profile,identifier&status=draft&_profile=http://medizininformatik-initiative.de/fhir/StructureDefinition/task-report-send-start|2.0](https://dic1/fhir/Task?_sort=_profile,identifier&status=draft&_profile=http://medizininformatik-initiative.de/fhir/StructureDefinition/task-report-send-start|2.0), select the process to be executed, add inputs if needed, and start the process.

Check transferred report to HRP:

```sh
curl -H "Accept: application/fhir+xml" \
--ssl-no-revoke --cacert cert/DSF_DEV_Root_CA.crt \
--cert cert/Webbrowser_Test_User.crt \
--key cert/Webbrowser_Test_User.key \
--pass password \
https://hrp/fhir/Bundle?identifier=http://medizininformatik-initiative.de/sid/cds-report-identifier|Test_DIC1
```

## End

Stop everything:

```sh
cd docker
docker-compose down -v
```
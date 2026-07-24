# Process Feasibility
## Preparations

Generate user specific dev setup files:

```sh
mvn dsf:generate-dev-setup-cert-files
```

Add entries to your hosts file:

```
127.0.0.1	dic1
127.0.0.1	dic2
127.0.0.1	hrp
```

In order for following commands to work, the process plugin folder `mii-process-feasibility-parent` must be located next to the mii dev setup folder `mii-processes-dev-setup`.

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

## DIC 2
### DIC 2: FHIR Store

Start DIC2 HAPI FHIR store or DIC1 BLAZE FHIR store:

```sh
docker-compose up -d dic2-fhir-store-hapi && docker-compose logs -f dic2-fhir-store-hapi
docker-compose up -d dic2-fhir-store-blaze && docker-compose logs -f dic2-fhir-store-blaze
```

Access to DIC2 FHIR store at http://localhost:8081/fhir.

Download and insert data into DIC2 FHIR store executing commands from sub-folder `mii-processes-dev-setup/data`.
(*Unfortunately the testdata does not work with a HAPI FHIR server*).

```sh
./insert-with-token.sh http://localhost:8081/fhir
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

Add a Measure-Library example Bundle to HRP DSF FHIR server:

```sh
curl -H "Accept: application/fhir+json" -H "Content-Type: application/fhir+json" \
-d @../mii-process-feasibility-parent/mii-process-feasibility/src/test/resources/fhir/feasibility-request-bundle-without-task.json \
--ssl-no-revoke --cacert cert/DSF_DEV_Root_CA.crt \
--cert cert/Webbrowser_Test_User.crt \
--key cert/Webbrowser_Test_User.key \
--pass password \
https://hrp/fhir/
```

## Process Execution

Open [https://hrp/fhir/Task?_sort=_profile,identifier&status=draft&_profile=http://medizininformatik-initiative.de/fhir/StructureDefinition/feasibility-task-request|0.0](https://hrp/fhir/Task?_sort=_profile,identifier&status=draft&_profile=http://medizininformatik-initiative.de/fhir/StructureDefinition/feasibility-task-request|0.0), select the process to be executed, add the Measure reference from the Bundle response above, and start the process.

Check feasibility result at HRP by reloading the task after process execution finished and opening [https://hrp/fhir/Task?_sort=-_lastUpdated&_profile=http://medizininformatik-initiative.de/fhir/StructureDefinition/feasibility-task-single-dic-result|0.0](https://hrp/fhir/Task?_sort=-_lastUpdated&_profile=http://medizininformatik-initiative.de/fhir/StructureDefinition/feasibility-task-single-dic-result|0.0)

## End

Stop everything

```sh
cd docker
docker-compose down -v
```
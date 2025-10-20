# Process Feasibility

Build the project from the root directory of this repository by executing the following command.

```sh
mvn clean package
```

Add entries to your hosts file

```
127.0.0.1	dic1
127.0.0.1	dic2
127.0.0.1	dms
127.0.0.1	hrp
```

*A total of six console windows are required. Start docker-compose commands for consoles 1 to 7 from
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

Console 5: Start HRP DSF FHIR server and wait till started

```sh
docker-compose up -d hrp-fhir && docker-compose logs -f hrp-fhir
```

Console 5: Disconnect from log output (Ctrl-C) if server started
Console 5: Start HRP DSF BPE server

```sh
docker-compose up -d hrp-bpe && docker-compose logs -f hrp-fhir hrp-bpe
````

<!-- EXECUTE PROCESS -->

Start curl commands in console 6 from root-folder:* `mii-processes-test-setup`. In order for the commands 
to be executed, the process plugin folder `mii-process-feasibility` must be located next to the test setup folder 
`mii-processes-test-setup`.

Console 6: Start Feasibility Request Task at HRP using the following command

```sh
curl -H "Accept: application/fhir+json" -H "Content-Type: application/fhir+json" \
-d @bundle/feasibility-request-bundle.json \
--ssl-no-revoke --cacert cert/ca/testca_certificate.pem \
--cert cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.pem \
--key cert/Webbrowser_Test_User/Webbrowser_Test_User_private-key.pem \
--pass password \
https://hrp/fhir/
```

Console 5: Check execute tasks are sent to DIC1 and DIC2 in the log output from console 5
Console 2: Check DIC1 receives execute task and send query to store in the log output from console 2
Console 1: Check DIC1 store executes query in the log output from console 2
Console 3: Check DIC2 receives execute task and send query to store in the log output from console 3
Console 4: Check DIC2 store executes query in the log output from console 4
Console 5: Check DIC1 and DIC2 results are received in the log output from console 5

console 6 : Check if the Feasibility Request Task at the HRP contains a Task.output with 
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

After that, you can query the [CapabilityStatement][1] of the inbox:

```sh
curl \
  --cacert ../mii-process-feasibility-tools/mii-process-feasibility-test-data-generator/cert/ca/testca_certificate.pem \
  --cert-type P12 \
  --cert ../mii-process-feasibility-tools/mii-process-feasibility-test-data-generator/cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.p12:password \
  -H accept:application/fhir+json \
  -s https://zars/fhir/metadata |\
  jq '.software, .implementation'
```

As you can see in the above command, a port mapping is created on port 443. In order to be able to access not only the
zars, but also the other sites, you have to use the domain name `zars` here. With [curl][2], you can specify a custom
resolver, which will resolve the host `zars` to localhost. An alternative would be to create an entry in
your `/etc/hosts`. The next line in the command is about Client and CA Certificates.

After that, you can stop the ZARS FHIR Inbox log output and start the ZARS Business Process Engine in the same terminal:

```sh
docker-compose up -d zars-bpe-app && docker-compose logs -f zars-fhir-app zars-bpe-app
```

After starting the ZARS, you can start the DIC-1 FHIR Inbox using:

```sh
docker-compose up -d dic-1-fhir-app && docker-compose logs -f dic-1-fhir-app
```

The following command should return the CapabilityStatement:

```sh
curl \
  --cacert ../mii-process-feasibility-tools/mii-process-feasibility-test-data-generator/cert/ca/testca_certificate.pem \
  --cert-type P12 \
  --cert ../mii-process-feasibility-tools/mii-process-feasibility-test-data-generator/cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.p12:password \
  -H accept:application/fhir+json \
  -s https://dic-1/fhir/metadata |\
  jq '.software, .implementation'
```

After that, you can stop the DIC-1 FHIR Inbox log output and start the DIC-1 Business Process Engine and Blaze in the same terminal:

```sh
docker-compose up -d dic-1-bpe-app && docker-compose logs -f dic-1-fhir-app dic-1-bpe-app
```

Continue with other DIC as you see fit.

After that we can POST the first Task to the ZARS:

```sh
curl \
  --ssl-no-revoke --cacert cert/ca/testca_certificate.pem \
  --cert cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.pem \
  --key cert/Webbrowser_Test_User/Webbrowser_Test_User_private-key.pem \
  -H accept:application/fhir+json \
  -H content-type:application/fhir+json \
  -d @bundle/feasibility-request-bundle.json \
  -s https://hrp/fhir/ |\
  jq .
```

After exporting the Task ID to $TASK_ID, you can fetch the task:

```sh
curl \
  --cacert ../mii-process-feasibility-tools/mii-process-feasibility-test-data-generator/cert/ca/testca_certificate.pem \
  --cert-type P12 \
  --cert ../mii-process-feasibility-tools/mii-process-feasibility-test-data-generator/cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.p12:password \
  -H accept:application/fhir+json \
  -s "https://hrp/fhir/Task/${TASK_ID}" |\
  jq .
```

The task should be completed and contain an output with the reference of the MeasureReport created.

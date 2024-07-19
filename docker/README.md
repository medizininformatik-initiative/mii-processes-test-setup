# Testing using Docker Setup

Before you start, create a copy of the file `docker-compose.override.yml.example` and name it `docker-compose.override.yml`.
Within this file you should configure the processes you want to test. 
For the following processes, some example configurations have already been added to `docker-compose.override.yml.example`.
If you want to use the `dic2-fhir-store-blaze` container, make sure to start the `keycloak` container first.

- To test the `Process Report`, follow the instructions in [README-Process-Report.md](./README-Process-Report.md)
- To test the `Process Data-Transfer`, follow the instructions in [README-Process-Data-Transfer.md](./README-Process-Data-Transfer.md)
- To test the `Process Data-Sharing`, follow the instructions in [README-Process-Data-Sharing.md](./README-Process-Data-Sharing.md)
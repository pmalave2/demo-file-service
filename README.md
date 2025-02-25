# demo-file-service

## First steps
* Clone this repository
## How to run

* Install [Docker](https://docs.docker.com/engine/install/)
* At project root, run:
```bash
docker compose up
```
<br />

The service will receive requests through the endpoint <a href="http://localhost:8081/">http://localhost:8081/</a>.
<br />

Use this <a href="postman/file-service.postman_collection.json">Postman Collection</a> (Environment <a href="postman/local.postman_environment.json">here</a>) to do calls to the endpoint using REST.
Use <a href="http://localhost:8082/">http://localhost:8082/</a> to access to the database instance, credential are [here](docker-compose.yml).
To see the BLOBs uploaded to Azurite, use [Azure Storage Explorer](https://azure.microsoft.com/en-us/products/storage/storage-explorer).

## Run tests
* Install [Java 21 JDK](https://adoptium.net/) and [Maven](https://maven.apache.org/download.cgi)
* run:
```bash
mvn clean verify
```

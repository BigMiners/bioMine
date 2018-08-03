# bioMine

bioMine is a full‐text natural language search engine for biomedical literature. 
bioMine provides search capabilities based on the full‐text content of documents belonging to a database composed of scientific articles and allows users to submit their search queries using natural language. 
Beyond the text content of articles, the system engine also uses article metadata, empowering the search by considering extra information from picture and table captions. 
bioMine is publicly released as an open‐source system under the MIT license. 


## Compiling from source 

bioMine source code requires [Maven](https://maven.apache.org) for compilation.

To compile from source, follow the next steps after cloning the repository: 

From the main bioMine project directory, compile the code and generate executable .jar files:
```bash 
mvn clean install
```

## Preparing Solr + environment

1. Install the bioLinker dependency
```bash
mvn install:install-file -Dfile=/path/to/bioLinker-1.0-SNAPSHOT.jar -DgroupId=csfg \
 -DartifactId=bioLinker -Dversion=1.0-SNAPSHOT -Dpackaging=jar       
```

> /path/to/bioLinker-1.0-SNAPSHOT.jar should be replaced by the path to the jar located in biomine-index/src/lib.

2. Download and unzip [Solr](http://lucene.apache.org/solr/mirrors-solr-latest-redir.html) (last update using 7.3.1)

3. Run SolrCloud by navigating to /path/to/solr/unzipped, and typing:
```bash
 bin/solr start -c
```

4. Create a collection using bioMine index schemas and configuration for Solr, use:
```bash
 bin/solr create -c <name of collection> -d /path/to/bioMine/solr/collectionConfigs/<name of collection folder>
```

5. Prepare a config.properties file with proper paths and configurations, using the [config.properties.DEFAULT](../config.properties.DEFAULT) file.
 
- The variable `server.url` is pointing to the active Solr address port (default is 8983)
`server.url=http://localhost:8983/solr`

- The variable ```collections``` should list the names of active collections that will be used
`collections=collection1,collection2`

- The variable ```shards``` should list all shard names that will be used
`shards=localhost:8983/solr/collection1_shard1_replica_n1,localhost:8983/solr/collection2_shard1_replica_n1`

## Running bioMine 

To run bioMine, use:
```bash 
java -DbioMine.config=config.properties -jar biomine-service/target/biomine-service-1.0-SNAPSHOT.jar
```

## Using bioMine 

After following the previous steps, bioMine will likely be running on localhost:8080.

Requests can be sent and received through CURL, such as

```bash
curl -X GET --header 'Accept: application/json' 'http://localhost:8080/biomine/indexer/index/status'
```
To access querying and indexing funcionalities through REST, we suggest using Swagger.

If running on localhost:8080, follow the next steps:

1. Go to ``` http://localhost:8080/swagger-ui.html```

2. Click on ```biomine-controller``` to expand this section

3. To use a funcionality, fill out the required fields, and click on ```try it out``` to submit a request


# bioMine

bioMine is a full‐text natural language search engine for biomedical literature. 
bioMine provides search capabilities based on the full‐text content of documents belonging to a database composed of scientific articles and allows users to submit their search queries using natural language. 
Beyond the text content of articles, the system engine also uses article metadata, empowering the search by considering extra information from picture and table captions. 
bioMine is publicly released as an open‐source system under the MIT license. 


## Compiling from source 

bioMine source code requires Maven (https://maven.apache.org) for compilation.
To compile from source, follow the next steps after cloning the repository: 

1 - From the main bioMine project directory, compile the code:
$~ mvn compile 
or
$~ mvn test-compile

2 - From the same directory, generate the executable .jar files:
$~ mvn package


## Running bioMine 

1 - Prepare a config.properties file with proper paths and configurations.
For this, use the config.properties.DEFAULT provided in the repository.

2 - Dowload and install/unzip Solr
(https://lucene.apache.org/solr/guide/6_6/getting-started.html)

3 - Run SolrCloud, following the instructions
(https://lucene.apache.org/solr/guide/7_1/getting-started-with-solrcloud.html)

4 - To run bioMine, use:
$~ java -DbioMine.config=config.properties -jar biomine-service/target/biomine-service-1.0-SNAPSHOT.jar


## Using bioMine 

After following the previous steps, bioMine will likely be running on localhost:8080.
Requests can be sent and received through CURL, such as

$~ curl -X GET --header 'Accept: application/json' 'http://localhost:8080/biomine/indexer/index/status'

To access querying and indexing funcionalities through REST, we suggest using Petstore Swagger.
If running on localhost:8080, follow the next steps:

1 - Go to http://petstore.swagger.io/
2 - In the main top bar, replace "https://petstore.swagger.io/v2/swagger.json" by "http://localhost:8080/v2/api-docs"
3 - Click "Explore"
4 - Expand the "biomine-controller" section
5 - Open one funcionality, and click in "Try it out" to access it

## Citing bioMine

Hayda Almeida, Ludovic Jean-Louis, Marie-Jean Meurs
[**An open source and modular search engine for biomedical literature retrieval**](http://labunix.uqam.ca/~meurs_m/publications/coin_2017.pdf), Computational Intelligence. July 2017 doi: 10.1111/coin.12125


```
@article{almeida2018open,
  title={An open source and modular search engine for biomedical literature retrieval},
  author={Almeida, Hayda and Jean-Louis, Ludovic and Meurs, Marie-Jean},
  journal={Computational Intelligence},
  volume={34},
  number={1},
  pages={200--218},
  year={2018},
  publisher={Wiley Online Library}
}
```


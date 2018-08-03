package com.engine.biomine.query;

import java.io.IOException;
import java.util.*;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.CommonParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.engine.biomine.common.Configs;
import com.engine.biomine.query.result.DocumentResult;
import com.engine.biomine.query.result.FacetItem;
import com.engine.biomine.query.result.FacetResult;
import com.engine.biomine.query.result.SearchResponse;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;


/**
 *
 * @author halmeida
 *
 */
public class QueryManager {

    private EDismaxQueryParser queryParser;
    private ArrayList<String> serverUrl;
    Properties props;
    private final CloudSolrClient solrClient;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String DEF_TYPE = "edismax";
    private boolean verbose = true;
    private final String defaultCollection;

    public QueryManager() {
        props = Configs.getInstance().getProps();
        this.serverUrl = new ArrayList<>(Arrays.asList(props.getProperty("server.url").split(",")));
        logger.info("Using Solr server {}", this.serverUrl);
//        this.solrClient= new CloudSolrClient.Builder(serverUrl).build();
        this.solrClient= new CloudSolrClient.Builder(serverUrl).build();

        String collection = props.getProperty("collections");
        defaultCollection = (collection.contains(",")) ? collection.substring(0, collection.indexOf(",")) : collection;
        solrClient.setDefaultCollection(defaultCollection);

        this.queryParser = new EDismaxQueryParser();
    }
    /**
     * Executes a solr query and provides retrieved documents
     * @param inputQuery string query input by user
     * @return list of solr documents
     */
    public QueryResponse processQuery(String inputQuery, String task, int startOffset, int numResult, boolean expandQuery, boolean highlights){

//        if (task.contains("eval")) expandQuery = false;
//		else expandQuery = Boolean.parseBoolean(props.getProperty("query.expansion"));

        SolrQuery bioMineQuery = setQueryParams(inputQuery, task, expandQuery);
        bioMineQuery.setStart(startOffset);
        bioMineQuery.setRows(numResult);
        bioMineQuery.setHighlight(highlights);

        //TO BE REMOVED
//	   logger.info("Query generated: " +bioMineQuery.toString());
        QueryResponse queryResp = null;

        try {
            logger.info("Sending query to Solr: \n [ {} ]", bioMineQuery.getQuery());
            queryResp = this.solrClient.query(bioMineQuery);
            logger.info("Query processed. {} results found in bioMine. ", queryResp.getResults().getNumFound());

        } catch (SolrServerException | IOException e) {
            logger.error("Could not process query " + bioMineQuery.getQuery(), e);
        }
        return queryResp;
    }

    public SearchResponse buildSearchResponse(QueryResponse queryResponse, List<String> fields, boolean highlight) {

        SearchResponse serviceResponse = new SearchResponse();
        long totalItemFound = queryResponse.getResults().getNumFound();

        //handle fields to display + highlights
        ArrayList<DocumentResult> docItems = getFieldsForResults(queryResponse, fields, highlight);

        //handle facets
//        serviceResponse.setFacets(getFacetsForResults(queryResponse));

        //docItems.size looks weird here...
        serviceResponse.setNumItemPerPage(docItems.size());
        serviceResponse.setTotalItemFound(totalItemFound);
        serviceResponse.setItems(docItems);

        logger.info("Found {} total hits for query", totalItemFound);
        logger.info("Returning {} results", docItems.size());

        return serviceResponse;
    }


    /**
     * Retrieve a solr query and defines query parameters
     *
     * @param inputQuery string query input by user
     * @return a solr query
     */
    private SolrQuery setQueryParams(String inputQuery, String task, boolean expandQuery) {

        HashMap<String, String> userParams = queryParser.getBioMineQuery(inputQuery, expandQuery);

        String thisQuery = userParams.get(queryParser.QUERY_PARAM);
        String qfParams = userParams.get(queryParser.QUERY_FIELDS);
        String pfParams = userParams.get(queryParser.PHRASE_FIELDS);
        String flParams = "";

        if (expandQuery) {
            logger.info("In " + task + " query expanded to:");
            logger.info(thisQuery);
        } else {
            logger.info("In " + task + "Query not expanded:");
            logger.info(thisQuery);
        }

        SolrQuery bioMineQuery = new SolrQuery(thisQuery);

        bioMineQuery.setParam(queryParser.TYPE_PARAM, DEF_TYPE);
        if (!qfParams.isEmpty()) {
       //     bioMineQuery.setParam(queryParser.QUERY_FIELDS, qfParams);
        }
        if (!pfParams.isEmpty()) {
       //     bioMineQuery.setParam(queryParser.PHRASE_FIELDS, pfParams);
        }
        if (!flParams.isEmpty()) {
            bioMineQuery.setParam(queryParser.DISPLAY_FIELDS, flParams);
        }

        bioMineQuery.add(queryParser.SHARDS, queryParser.shards);
        bioMineQuery.add(CommonParams.SORT, "score " + SolrQuery.ORDER.desc.toString());

        bioMineQuery.add(queryParser.QUERY_SLOP, Integer.toString(queryParser.QUERY_SLOP_FACTOR));
        bioMineQuery.add(queryParser.PHRASE_SLOP, Integer.toString(queryParser.PHRASE_SLOP_FACTOR));

        if (verbose) {
            logger.info("Solr Query: " + bioMineQuery.toString());
        }

        return bioMineQuery;
    }


    /**
     * Display in response only fields selected by user
     * plus highlights if applicable
     * @param doc
     * @param fields
     * @return
     */
    private ArrayList<DocumentResult> getFieldsForResults(QueryResponse response, List<String> fields, boolean highlight){

        ArrayList<DocumentResult> results = new ArrayList<>();

        for(SolrDocument doc : response.getResults()) {
            DocumentResult result = new DocumentResult();
            String id = (String) doc.getFieldValue("id");

            Iterator<String> iter = doc.getFieldNames().iterator();
            while(iter.hasNext()){
                String field  = iter.next();
                if(fields.contains(field) || fields.contains("*")){
                    Object value = doc.getFieldValue(field);
                    result.addField(field, value);
                }
            }
            // this code would allow only to add fields from one document ty
//            for (String field : fields) {
//                if (doc.getFieldNames().contains(field)) {
//                    Object value = doc.getFieldValue(field);
//                    result.addField(field, value);
//                }
//            }

            if(highlight) result.setHighlightSnippets(response.getHighlighting().get(id));

            results.add(result);
        }
        return results;
    }


    private List<FacetResult> getFacetsForResults(QueryResponse queryResponse){
        List<FacetResult> facets = new ArrayList<>();

        for (FacetField facet : queryResponse.getFacetFields()) {
            String facetLabel = facet.getName();
            List<FacetItem> facetItems = new ArrayList<>();

            for (Count facetCount : facet.getValues()) {
                FacetItem facetItem = new FacetItem(facetCount.getName(), facetCount.getCount());
                facetItems.add(facetItem);
            }
            FacetResult categorytResults = new FacetResult(facetLabel, facetItems);
            facets.add(categorytResults);
        }
        return facets;
    }

    //    /**
//     * Returns list of x SolrDocuments for given query (outputs IDs retrieved)
//     *
//     * @param inputQuery user query
//     * @param task if eval, query is not expanded
//     * @return solrdocument list
//     */
//    public List<SolrDocument> getResultsForQuery(String inputQuery, String task) {
//        List<SolrDocument> results = processQuery(inputQuery, task).getResults();
//        Iterator<SolrDocument> docIterator = results.iterator();
//        int count = 1;
//
//        logger.info("Evaluating first " + results.size() + " ranked documents...");
//
//        while (docIterator.hasNext()) {
//            SolrDocument doc = docIterator.next();
//            count++;
//        }
//        return results;
//    }

    /**
     *
     * @param response
     * @return
     */
//    private DocumentResult getHighlightsForResults(QueryResponse response){

//        ArrayList<DocumentResult> docItems = new ArrayList<>();
//        SolrDocumentList solrList = response.getResults();

//        if(response.getHighlighting() != null && doc.size() > 0) {


//                    for (int i = 0; i < solrList.size(); i++) {

//                SolrDocument resultDoc = solrList.get(i);

//                DocumentResult docItem = getFieldsForResults(resultDoc, fields);
//                String docId = (String) resultDoc.getFieldValue("id");

    //get highlights for doc
//                Map<String, List<String>> highlightSnippets = response.getHighlighting().get(docId);
//                docItem.setHighlightSnippets(highlightSnippets);
    //return the matched text
//                docItem.addHighlightTerms(highlightSnippets);
//
//                docItems.add(docItem);
//            }
//        }
//        return docItems;
//    }


//    public SearchResponse processQuery(String inputQuery, int startOffset, int numResult, boolean expandQuery) {
//        QueryResponse queryResp = executeQuery(inputQuery, startOffset, numResult, expandQuery);
//        //convert queryResponse to searchResponse
//        SearchResponse response = buildSearchResponse(queryResp);
//        return response;
//    }

    //        public QueryResponse executeQuery(String inputQuery, int startOffset, int numResult,boolean expandQuery) {
//        SolrQuery bioMineQuery = setQueryParams(inputQuery, "");
//        //add pagination information to the query
//        bioMineQuery.setStart(startOffset); //starting offset
//        bioMineQuery.setRows(numResult); //number of results to return
//
//        QueryResponse queryResp = null;
//
//        try {
//            queryResp = this.solrClient.query(bioMineQuery);
//            logger.info("Sending query {} to Solr", bioMineQuery.toString());
//            logger.info("Query processed. {} results found in bioMine.", queryResp.getResults().getNumFound());
//
//        } catch (SolrServerException | IOException e) {
//            logger.error("Could not process query {}", bioMineQuery.getQuery(), e);
//        }
//
//
//        return queryResp;
//    }
}

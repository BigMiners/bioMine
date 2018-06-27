package com.engine.biomine.query;

import com.engine.biomine.common.Configs;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SimpleParams;
import org.apache.solr.common.util.StrUtils;

import com.engine.biomine.common.FIELDS;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a lucene Solr query after handling a complex (NL) query typed by a
 * user Query have one of the three formats: 1 keyword query (Kq) 2 statement
 * query (Sq) 3 open question query (Oq)
 *
 * @author halmeida
 *
 */
public class LuceneQueryParser {

    private final Configs biomineConfig;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    //stop-words file name
    String pathStopWords = "queryStopwords";
    String pathQuestionWords = "questionWords";
    List<String> stopWordsList = new ArrayList<String>();
    List<String> questionWordsList = new ArrayList<String>();
    List<String> queryFields = new ArrayList<String>();

    final String KEYWORD_QUERY = "keyword";
    final String STATEMENT_QUERY = "statement";
    final String QUESTION_QUERY = "openquestion";
    final int BOOSTING_FACTOR = 5;

    public LuceneQueryParser() {
        biomineConfig = new Configs();
        loadQueryFields();
        loadStopWordsList();
        loadQuestionWordsList();
    }

//	public static void main(String[] args){
//		
//		List<String> query = new ArrayList<String>();
//		
//		SolrQuery userQuery = new SolrQuery();
//				
////		query.add("peptide");
//		query.add("vector");
////		query.add("that");
////		query.add("with");
////		query.add("gene");
////		query.add("data");		
//		query.add("tropism");
////		query.add("2014");
//		query.add("?");
//		
//		BQueryParser qh = new BQueryParser();
//		
//		userQuery = qh.getSolrQuery(query);
//		
//		System.out.println("Type: " + qh.findQueryType(query));		
//		System.out.println("Solr query: " + userQuery.getQuery());		
//	}	
    /**
     * Processes a user query into a question query, statement query or keyword
     * query and provides a Solr query
     *
     * @param query terms provided by user
     * @return a solr query
     */
//	public SolrQuery getSolrQuery(List<String> query){
    public String getSolrQuery(List<String> query) {

//		SolrQuery solrQuery = new SolrQuery();
        String solrQuery = "";

        query = removeDuplicates(query);
        String type = findQueryType(query);

        switch (type) {

            case QUESTION_QUERY:
                solrQuery = getQuestionQuery(query);
                break;

            case STATEMENT_QUERY:
                solrQuery = getStatementQuery(query);
                break;

            case KEYWORD_QUERY:
                solrQuery = getKeywordQuery(query);
                break;
        }
        return solrQuery;
    }

    /**
     * Re-writes a user statement query into a solr query
     *
     * @param query terms provided by user
     * @return a statement (solr) query
     */
    public String getStatementQuery(List<String> query) {

        ModifiableSolrParams params = new ModifiableSolrParams();

        //separate stopwords from query terms
        List<String> queryStopWords = removeStopWords(query, "extract");
        query = removeStopWords(query, "");

        for (int i = 0; i < queryFields.size(); i++) {
            //for body content
            if (queryFields.get(i).contains(FIELDS.body)) {
                //boosted proximity				
                params.set(queryFields.get(i), getBoostClause(getProximityClause(query, query.size() - 1)));
            } //for article title
            else if (queryFields.get(i).contains(FIELDS.articleTitle)) {

                //boosted AND + OR for stopwords
                String queryStopClause = getBooleanClause(queryStopWords, SimpleParams.OR_OPERATOR);
                String queryClause = getBooleanClause(query, SimpleParams.AND_OPERATOR);
                String titleClause = "(" + queryStopClause + " " + SimpleParams.OR_OPERATOR + " " + queryClause + ")";

                params.set(queryFields.get(i), getBoostClause(titleClause));
            } //for abstract and reference list
            else if (queryFields.get(i).contains(FIELDS.abs)
                    || queryFields.get(i).contains(FIELDS.referenceTitle)) {
                //boolean AND - stopwords						
                params.set(queryFields.get(i), getBooleanClause(removeStopWords(query, ""), SimpleParams.AND_OPERATOR));
            } //for all other fields
            else {
                //OR clause for all terms//				
                //boost if terms are all numeric
                if ((queryFields.get(i).contains(FIELDS.year)
                        || queryFields.get(i).contains(FIELDS.id))
                        && isNumeric(query)) {
                    params.set(queryFields.get(i), getBoostClause(getBooleanClause(query, SimpleParams.OR_OPERATOR)));
                } else {
                    params.set(queryFields.get(i), getBooleanClause(query, SimpleParams.OR_OPERATOR));
                }
            }
        }

        return params.toString();
//		return new SolrQuery (params.toString());

    }

    /**
     * Re-writes a user keyword query into a solr query
     *
     * @param query terms provided by user
     * @return a keyword (solr) query
     */
//	public SolrQuery getKeywordQuery(List<String> query){
    public String getKeywordQuery(List<String> query) {

        ModifiableSolrParams params = new ModifiableSolrParams();

        for (int i = 0; i < queryFields.size(); i++) {
            //for article title and author names
            if (queryFields.get(i).contains(FIELDS.articleTitle)
                    || queryFields.get(i).contains(FIELDS.author)) {
                //boosted proximity 				
                params.set(queryFields.get(i), getBoostClause(getProximityClause(query, query.size() - 1)));
            } //for article keywords
            else if (queryFields.get(i).contains(FIELDS.keywords)) {
                //boosted OR 				
                params.set(queryFields.get(i), getBoostClause(getBooleanClause(query, SimpleParams.OR_OPERATOR)));
            } //for abstract, full content, author names
            else if (queryFields.get(i).contains(FIELDS.author)
                    || queryFields.get(i).contains(FIELDS.abs)
                    || queryFields.get(i).contains(FIELDS.body)) {
                params.set(queryFields.get(i), getProximityClause(query, query.size() - 1));
            } //for all other fields
            else {
                //OR clause for all terms				
                //boost if terms are all numeric
                if ((queryFields.get(i).contains(FIELDS.year)
                        || queryFields.get(i).contains(FIELDS.id))
                        && isNumeric(query)) {
                    params.set(queryFields.get(i), getBoostClause(getBooleanClause(query, SimpleParams.OR_OPERATOR)));
                } else {
                    params.set(queryFields.get(i), getBooleanClause(query, SimpleParams.OR_OPERATOR));
                }
            }
        }

        return params.toString();
//		return new SolrQuery (params.toString());
    }

    /**
     * Re-writes a user question query into a solr query
     *
     * @param query terms provided by user
     * @return a question (solr) query
     */
//	public SolrQuery getQuestionQuery (List<String> query){
    public String getQuestionQuery(List<String> query) {

        ModifiableSolrParams params = new ModifiableSolrParams();

        //remove stopwords if any
        if (hasStopWords(query)) {
            query = removeStopWords(query, "");
        }
        //remove question terms
        query = removeQuestionWords(query);

        for (int i = 0; i < queryFields.size(); i++) {
            //for body content
            if (queryFields.get(i).contains(FIELDS.body)) {
                // boosted OR clause for all terms	
                String bodyClause = "(" + getBooleanClause(query, SimpleParams.OR_OPERATOR) + ")";
                params.set(queryFields.get(i), getBoostClause(bodyClause));
            } //for abstract content
            else if (queryFields.get(i).contains(FIELDS.abs)) {
                // AND clause for all terms
                params.set(queryFields.get(i), getBooleanClause(query, SimpleParams.AND_OPERATOR));
            } //for all other fields
            else {
                //OR clause for all terms								
                params.set(queryFields.get(i), getBooleanClause(query, SimpleParams.OR_OPERATOR));
            }
        }
        return params.toString();
//		return new SolrQuery (params.toString());
    }

    /**
     * Generates a boolean query clause using terms provided by user
     *
     * @param query user query terms
     * @param operator boolean operator to be used (OR, AND)
     * @return a boolean clause using user query terms
     */
    public String getBooleanClause(List<String> query, String operator) {

        StringBuffer booleanClause = new StringBuffer();

        for (int i = 0; i < query.size(); i++) {
            if (i < query.size() - 1) {
                booleanClause.append(query.get(i) + " " + operator + " ");
            } else {
                booleanClause.append(query.get(i));
            }
        }
        return booleanClause.toString();
    }

    /**
     * Generates a proximity query clause using terms provided by user
     *
     * @param query user query terms
     * @param proximity_factor int factor to be considered
     * @return a proximity clause using provided factor
     */
    public String getProximityClause(List<String> query, int proximity_factor) {

        StringBuffer proximityClause = new StringBuffer();
        proximityClause.append("\"");

        for (int i = 0; i < query.size(); i++) {
            if (i < query.size() - 1) {
                proximityClause.append(query.get(i) + " ");
            } else {
//				if(proximity_factor >= 1 )
//					proximityClause.append(query.get(i) +"\"~"+ proximity_factor);
//				else 
                proximityClause.append(query.get(i) + "\"");
            }
        }
        return proximityClause.toString();
    }

    /**
     * Adds boosting factor to a query clause
     *
     * @param query
     * @return
     */
    public String getBoostClause(String query) {
        return query; // + "^" + BOOSTING_FACTOR;		
    }

    /**
     * Checks if a query has only numeric terms
     *
     * @return true if only numeric terms in query
     */
    public boolean isNumeric(List<String> query) {

        for (int i = 0; i < query.size(); i++) {
            if (!query.get(i).matches("[-+]?\\d*\\.?\\d+")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes duplicate terms in a query
     *
     * @param query terms
     * @return query without duplicates
     */
    public List<String> removeDuplicates(List<String> query) {

        List<String> queryNoDuplicates = new ArrayList<String>();

        for (int i = 0; i < query.size(); i++) {
            boolean insert = true;

            for (int j = i + 1; j < query.size(); j++) {
                if (query.get(i).equalsIgnoreCase(query.get(j))) {
                    insert = false;
                }
            }
            if (insert) {
                queryNoDuplicates.add(query.get(i));
            }
        }
        return queryNoDuplicates;
    }

    /**
     * Returns one of the three query types for a given query
     *
     * @param query terms
     * @return query type
     */
    public String findQueryType(List<String> query) {

        String type = "";

        if (!hasQuestionWords(query)) {
            if (!hasStopWords(query)) {
                type = KEYWORD_QUERY;
            } else {
                type = STATEMENT_QUERY;
            }
        } else {
            type = QUESTION_QUERY;
        }
        return type;
    }

    /**
     * Loads all schema fields used by Solr
     */
    public void loadQueryFields() {

        FIELDS fields = new FIELDS();

        queryFields.add(fields.id);
        queryFields.add(fields.ji);
        queryFields.add(fields.ui);
        queryFields.add(fields.fm);
        queryFields.add(fields.bm);
        queryFields.add(fields.abs);
        queryFields.add(fields.body);
        queryFields.add(fields.journalTitle);
        queryFields.add(fields.pmid);
        queryFields.add(fields.pmc);
        queryFields.add(fields.articleTitle);
        queryFields.add(fields.author);
        queryFields.add(fields.keywords);
//		queryFields.add(fields.sections);
        queryFields.add(fields.referenceTitle);
        queryFields.add(fields.referenceId);
        queryFields.add(fields.referenceAuthor);
        queryFields.add(fields.year);
    }

    /**
     * Reads stopwords list file
     */
    public void loadStopWordsList() {

        try {
            pathStopWords = biomineConfig.getProps().getProperty("queryStopwords", "queryStopwords");
            if (new File(pathStopWords).exists() == false) {
                logger.error("Check that queryStopwords file exists in the your classpath or that you have set the 'queryStopwords' parameter in the properties");
                System.exit(0);
            }
            BufferedReader reader = new BufferedReader(new FileReader(pathStopWords));
            String line = null;

            //loading stop-words list
            while ((line = reader.readLine()) != null) {
                stopWordsList = StrUtils.splitSmart(line, ',');
                line = reader.readLine();
            }
            reader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads question words list file
     */
    public void loadQuestionWordsList() {

        try {
            pathQuestionWords = biomineConfig.getProps().getProperty("questionWords", "questionWords");
            if (new File(pathQuestionWords).exists() == false) {
                logger.error("Check that questionWords file exists in you classpath or that you have set the 'questionWords' parameter in the properties");
                System.exit(0);
            }
            BufferedReader reader = new BufferedReader(new FileReader(pathQuestionWords));
            String line = null;

            //loading question words list
            while ((line = reader.readLine()) != null) {
                questionWordsList = StrUtils.splitSmart(line, ',');
                line = reader.readLine();
            }
            reader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if a given query contains any question words
     *
     * @param query terms
     * @return true if question word is found
     */
    public boolean hasQuestionWords(List<String> query) {

        for (int i = 0; i < query.size(); i++) {
            if (questionWordsList.contains(query.get(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a given query contains any stop words
     *
     * @param query terms
     * @return true if stopwords found
     */
    public boolean hasStopWords(List<String> query) {

        for (int i = 0; i < query.size(); i++) {
            if (stopWordsList.contains(query.get(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes question words from query given the list of question words
     *
     * @param query terms
     * @return query with no question words
     */
    public List<String> removeQuestionWords(List<String> query) {

        List<String> resultQuery = new ArrayList<String>();

        for (int i = 0; i < query.size(); i++) {

            if (!questionWordsList.contains(query.get(i))) {
                resultQuery.add(query.get(i));
            }
        }
        return resultQuery;
    }

    /**
     * Removes stopwords from query given the list of stopwords
     *
     * @param query terms
     * @return query with no stopwords
     */
    public List<String> removeStopWords(List<String> query, String task) {

        List<String> result = new ArrayList<String>();

        for (int i = 0; i < query.size(); i++) {

            if (task.contains("extract")) {
                if (stopWordsList.contains(query.get(i))) {
                    result.add(query.get(i));
                }
            } else if (!stopWordsList.contains(query.get(i))) {
                result.add(query.get(i));
            }
        }
        return result;

    }

}

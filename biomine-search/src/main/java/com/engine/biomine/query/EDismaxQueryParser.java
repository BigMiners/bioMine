package com.engine.biomine.query;

import com.engine.biomine.common.Configs;
import java.util.*;
import com.engine.biomine.common.IOUtil;
import com.engine.biomine.queryannotation.MetaMapAnnotator;
import com.engine.biomine.common.FIELDS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a edismax Solr query after handling
 * a complex (NL) query typed by a user
 * Query have one of the three formats: 
 * 1 keyword query (Kq)
 * 2 statement query (Sq)
 * 3 open question query (Oq)
 * 
 * @author halmeida
 * 
 */
public class EDismaxQueryParser {
	private final Properties props;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
        
	 //stop-words file name
	String pathStopWords;
	String pathQuestionWords;
	List<String> stopWordsList;
	List<String> questionWordsList;
	List<String> queryFields = new ArrayList<String>();
	String shards = "";
	String cluster = "";
		
	final String KEYWORD_QUERY = "keyword";
	final String STATEMENT_QUERY = "statement";
	final String QUESTION_QUERY = "openquestion";
	final int BOOSTING_FACTOR = 2;
	final int PHRASE_SLOP_FACTOR = 30;
	final int QUERY_SLOP_FACTOR = 15;
	
	final String TYPE_PARAM = "defType";
	final String QUERY_PARAM = "q";
	final String QUERY_FIELDS = "qf";
	final String PHRASE_FIELDS = "pf";
	final String QUERY_SLOP = "qs";
	final String PHRASE_SLOP = "ps";
	final String DISPLAY_FIELDS = "fl";
	final String SHARDS = "shards";

	 //Returns the number of times the term appears in the field for that doc.
    private final String TF_PARAM = "tf"; //FLOAT
    //Returns the number of documents that contain the term in the field. This is a constant
    private final String DOC_FREQ_PARAM = "docfreq";
    //Inverse doc frequency; a measure of whether the term is common or rare across all documents.
    private final String IDF_PARAM = "idf"; //DOUBLE
	
	public EDismaxQueryParser(){
		props = Configs.getInstance().getProps();

		cluster = props.getProperty("server.url");
		shards = props.getProperty("shards");

		pathStopWords = props.getProperty("queryStopwords.path");
		pathQuestionWords = props.getProperty("questionWords.path");

		stopWordsList = IOUtil.getINSTANCE().loadFileWithSeparator(pathStopWords, true, ',');
		questionWordsList = IOUtil.getINSTANCE().loadFileWithSeparator(pathQuestionWords, true, ',');

		loadQueryFields();
	}


	/**
	 * Processes a user query into a
	 * question query, statement query or keyword query
	 * and provides a bioMine query
	 * 
	 * @param inputQuery query terms provided by user
	 * @return a bioMine query
	 */
	public HashMap<String,String> getBioMineQuery(String inputQuery, boolean expand){
		
		List<String> query = Arrays.asList(inputQuery.split(" "));
		
		HashMap<String,String> bioMineQuery = null;
				
		query = removeDuplicates(query);
		String type = findQueryType(query);
		
		switch(type){
		
		case QUESTION_QUERY: 
			bioMineQuery = getQuestionQuery(query);
			break;
		
		case STATEMENT_QUERY:
			bioMineQuery = getStatementQuery(query);
			break;
		
		case KEYWORD_QUERY:
			bioMineQuery = getKeywordQuery(query);
			break;
		}	

		if(expand) bioMineQuery = getBioMineQueryConcepts(bioMineQuery);

		return bioMineQuery;		
	}
			
	
	/**
	 * Adds concepts (if any) and 
	 * rules (if any concepts)
	 * to a bioMine Query
	 *  
	 * @param bioMineQuery params to be edited if concepts are found
	 * @return updated bioMine Query params with concepts and specific rules
	 */
	private HashMap<String,String> getBioMineQueryConcepts(HashMap<String,String> bioMineQuery){

		String userQuery = bioMineQuery.get(QUERY_PARAM).toLowerCase();
//		String conceptQuery = getConceptQuery(bioMineQuery.get(QUERY_PARAM).toLowerCase());
		String conceptQuery = getConceptQuery(bioMineQuery.get(QUERY_PARAM));

		//only apply further changes if concepts were found
		if(!userQuery.equalsIgnoreCase(conceptQuery)){		
			String queryFields = getConceptQueryFields(bioMineQuery.get(QUERY_FIELDS));
			String phraseFields = getConceptPhraseFields(bioMineQuery.get(PHRASE_FIELDS)); 

			bioMineQuery.put(QUERY_PARAM, conceptQuery);
			bioMineQuery.put(QUERY_FIELDS, queryFields);
			bioMineQuery.put(PHRASE_FIELDS, phraseFields);

		}

		return bioMineQuery;
	}
	

	/**
	 * Adds new parameters to query Phrase Fields 
	 * (boosts score of docs if all terms in 
	 * the q parameter appear close)
	 *  
	 * @param phraseFields
	 * @return phraseFields new params 
	 */
	private String getConceptPhraseFields(String phraseFields){

		phraseFields = phraseFields.replace("^"+BOOSTING_FACTOR, "");		

		//keywords will be searched as phrase concepts were found
		if(!phraseFields.contains(FIELDS.keywords))	phraseFields += " " + getExtraBoostClause(FIELDS.keywords);				
		else phraseFields = phraseFields.replace(FIELDS.keywords, getExtraBoostClause(FIELDS.keywords));

		if(!phraseFields.contains(FIELDS.abs)) phraseFields += " " + getBoostClause(FIELDS.abs);
		else phraseFields = phraseFields.replace(FIELDS.abs, getBoostClause(FIELDS.abs));

		if(!phraseFields.contains(FIELDS.body)) phraseFields += " " + getBoostClause(FIELDS.body);
		else phraseFields = phraseFields.replace(FIELDS.body, getBoostClause(FIELDS.body));	

		if(!phraseFields.contains(FIELDS.articleTitle)) phraseFields += "" + getBoostClause(FIELDS.articleTitle);
		else phraseFields = phraseFields.replace(FIELDS.articleTitle, getExtraBoostClause(FIELDS.articleTitle));

		return phraseFields;
	}
	
	/**
	 * Adds new parameters to Query Fields 
	 * (specifies fields on which to perform query)
	 *
	 * @param queryFields
	 * @return
	 */
	private String getConceptQueryFields(String queryFields) {

		queryFields = queryFields.replace("^"+BOOSTING_FACTOR, "");

		//keywords will be searched if concepts were found
		if(!queryFields.contains(FIELDS.keywords)) queryFields += " " + getExtraBoostClause(FIELDS.keywords);				
		else queryFields = queryFields.replace(FIELDS.keywords, getExtraBoostClause(FIELDS.keywords));

		return queryFields;

	}
		
	/**
	 * Retrieves UMLS concepts for userQuery
	 * Concepts are annotated with MetaMap
	 * @param userQuery
	 * @return annotated user query
	 */
	private String getConceptQuery(String userQuery){
		
		MetaMapAnnotator annot = new MetaMapAnnotator();
		String annotatedQuery =  annot.addMetaMapConcepts(userQuery);		

		return annotatedQuery;		
	}
		
	/**
	 * Re-writes a user statement query
	 * into a bioMine query
	 * 
	 * @param query terms provided by user
	 * @return a statement (bioMine) query
	 */
	private HashMap<String,String> getStatementQuery(List<String> query){
		
		HashMap<String,String> result = new HashMap<String,String>();
		
		StringBuilder queryFields = new StringBuilder();
		StringBuilder phraseFields = new StringBuilder();
		
		//remove stopwords from query terms		
		query = handleStopWords(query,"");
				
		phraseFields.append(getBoostClause(FIELDS.body) + " ");
		phraseFields.append(FIELDS.abs + " ");
		phraseFields.append(FIELDS.captions + " ");
		phraseFields.append(getBoostClause(FIELDS.articleTitle));

		queryFields.append(getBoostClause(FIELDS.body) + " ");
		queryFields.append(getBoostClause(FIELDS.articleTitle) + " ");
		queryFields.append(getBoostClause(FIELDS.abs) + " ");
		queryFields.append(getBoostClause(FIELDS.captions) + " ");
//		queryFields.append(FIELDS.referenceTitle + " "); //add smaller boost for references?
		queryFields.append(FIELDS.keywords + " ");
//		queryFields.append(FIELDS.journalTitle + " ");
		queryFields.append(FIELDS.author);
		
		int queryNumeric = hasNumeric(query); 
		
		if(queryNumeric > 0){
			if(queryNumeric <= 4 && queryNumeric % 2 == 0){
				queryFields.append(" " + getBoostClause(FIELDS.year) + " ");
			}
			else{
				queryFields.append(" " + getBoostClause(FIELDS.pmid) + " ");
				queryFields.append(getBoostClause(FIELDS.pmc));
			}
		}		
			
		String queryContent = removeSpecialChar(query.toString());
		
		result.put(QUERY_PARAM, queryContent);
		result.put(QUERY_FIELDS, queryFields.toString());
		result.put(PHRASE_FIELDS, phraseFields.toString());
				
		return result;		
	}
	
	/**
	 * Re-writes a user keyword query
	 * into a bioMine query
	 * 
	 * @param query terms provided by user
	 * @return a keyword (bioMine) query
	 */
	private HashMap<String,String> getKeywordQuery(List<String> query){
		
		HashMap<String,String> result = new HashMap<String,String>();
		
		StringBuilder queryFields = new StringBuilder();
		StringBuilder phraseFields = new StringBuilder();
				
		phraseFields.append(getBoostClause(FIELDS.articleTitle) + " ");
		phraseFields.append(getBoostClause(FIELDS.author) + " ");
		phraseFields.append(FIELDS.abs + " ");
		phraseFields.append(FIELDS.body);
		
		queryFields.append(getBoostClause(FIELDS.keywords) + " ");
		queryFields.append(getBoostClause(FIELDS.captions) + " ");
		queryFields.append(getBoostClause(FIELDS.abs) + " ");
		queryFields.append(getBoostClause(FIELDS.body) + " ");
		queryFields.append(FIELDS.articleTitle + " ");
//		queryFields.append(FIELDS.referenceTitle + " ");
		queryFields.append(FIELDS.author + " ");
//		queryFields.append(FIELDS.journalTitle);

		
		int queryNumeric = hasNumeric(query); 

		if(queryNumeric > 0){
			if(queryNumeric <= 4 && queryNumeric % 2 == 0){
				queryFields.append(" " + getBoostClause(FIELDS.year) + " ");
			}
			else{
				queryFields.append(" " + getBoostClause(FIELDS.pmid) + " ");
				queryFields.append(getBoostClause(FIELDS.pmc));
			}
		}		
		
		String queryContent = removeSpecialChar(query.toString());
				
		result.put(QUERY_PARAM, queryContent);
		result.put(QUERY_FIELDS, queryFields.toString());
		result.put(PHRASE_FIELDS, phraseFields.toString());
		
		return result;
	}
	
	/**
	 * Re-writes a user question query
	 * into a bioMine query
	 * 
	 * @param query terms provided by user
	 * @return a question (bioMine) query
	 */	
	private HashMap<String,String> getQuestionQuery (List<String> query){

		HashMap<String,String> result = new HashMap<String,String>();
		
		StringBuilder queryFields = new StringBuilder();
		StringBuilder phraseFields = new StringBuilder();
		
		//remove question terms
		query = removeQuestionWords(query);
		query = handleStopWords(query, "");
			
		phraseFields.append(getBoostClause(FIELDS.body) + " ");
		phraseFields.append(getBoostClause(FIELDS.abs) + " ");
		phraseFields.append(FIELDS.captions + " ");
		phraseFields.append(FIELDS.articleTitle);
		
		queryFields.append(getBoostClause(FIELDS.abs)+ " ");
		queryFields.append(getBoostClause(FIELDS.body) + " ");
		queryFields.append(getBoostClause(FIELDS.captions) + " ");
		queryFields.append(getBoostClause(FIELDS.articleTitle) + " ");
		queryFields.append(FIELDS.keywords + " ");
		queryFields.append(FIELDS.author + " ");
//		queryFields.append(FIELDS.journalTitle + " ");
//		queryFields.append(FIELDS.referenceTitle);

				
		int queryNumeric = hasNumeric(query);
		
		if(queryNumeric > 0){
			if(queryNumeric <= 4 && queryNumeric % 2 == 0){
				queryFields.append(" " + getBoostClause(FIELDS.year) + " ");
			}
			else{
				queryFields.append(" " + getBoostClause(FIELDS.pmid) + " ");
				queryFields.append(getBoostClause(FIELDS.pmc));
			}
		}		

		String queryContent = removeSpecialChar(query.toString());
		
		result.put(QUERY_PARAM, queryContent);
		result.put(QUERY_FIELDS, queryFields.toString());
		result.put(PHRASE_FIELDS, phraseFields.toString());
		
		return result;
	}
	
	
	/**
	 * Removes special characters for 
	 * tokenization process
	 * 
	 * @param str text to be cleaned
	 * @return cleaned string
	 */
	private String removeSpecialChar(String str){
				
//		if(!str.contains("'s")){
//			str = str.replace(".", "");
//			str = str.replace("'", "");
//		}
		
		if(str.contains(",")){			
			if(!(str.startsWith(" ", str.indexOf(",")+1)))
				str = str.replace(",", " ");
			else str = str.replace(",", "");			
		}	
		
		str = str.replace("}", "");
		str = str.replace("{", "");
		str = str.replace("]", "");
		str = str.replace("[", "");		
		str = str.replace("\"", "");
		str = str.replace("<", "");
		str = str.replace(">", "");
		str = str.replace("/", " ");
//		str = str.replace("\\", " ");
		str = str.replace("#", "");
		str = str.replace("*", "");
		str = str.replace("&gt", "");
		str = str.replace("&apos", "");
//		str = str.replace("%", "");
		str = str.replace("&quot", "");
		str = str.replace("&", "");
		str = str.replace("=", "");
		str = str.replace("?", "");
		str = str.replace("!", "");
		str = str.replace(";", "");
		str = str.replace(":", "");				
//		str = str.replace(")", "");
//		str = str.replace("(", "");
		str = str.replace("\t\t", "\t");
		str = str.replace("+", "");
		//losing ngrams because of hifen between names 
//		str = str.replace("-", " ");
		str = str.replace("  ", " ");
		
		return str;
	}
	
	/**
	 * Generates a boolean query clause
	 * using terms provided by user
	 * 
	 * @param query user query terms
	 * @param operator boolean operator to be used (OR, AND)
	 * @return a boolean clause using user query terms
	 */
	private String getBooleanClause (List<String> query, String operator){

		StringBuffer booleanClause = new StringBuffer();		
				
		for(int i = 0; i < query.size(); i ++){		
			if(i < query.size()-1)
				booleanClause.append(query.get(i) + " " + operator + " ");
			else 
				booleanClause.append(query.get(i));
		}		
		return booleanClause.toString();
	}
	
	/**
	 * Generates a proximity query clause
	 * using terms provided by user
	 * 
	 * @param query user query terms
	 * @param proximity_factor int factor to be considered 
	 * @return a proximity clause using provided factor
	 */
	private String getProximityClause(String query, int proximity_factor){

		if(proximity_factor >= 1 )
			query = query + "~"+ proximity_factor;
		
		return query;		
	}
	
	/**
	 * Adds boosting factor to a query clause
	 * @param query
	 * @return
	 */	
	private String getBoostClause (String query){
		return query + "^" + BOOSTING_FACTOR;		
	}
	
	private String getExtraBoostClause (String query){
		return query + "^" + BOOSTING_FACTOR*2;		
	}
	
	/**
	 * Checks if a query has any numeric terms
	 * @return numeric term length
	 */
	private int hasNumeric(List<String> query){
		
		for(int i = 0; i < query.size(); i ++){	
			if(query.get(i).matches("[-+]?\\d*\\.?\\d+")){
				return query.get(i).length();
			}
		}		
		return 0;
	}
	
	/**
	 * Removes duplicate terms in a query
	 * 
	 * @param query terms
	 * @return query without duplicates
	 */
	private List<String> removeDuplicates(List<String> query){
		
		List<String> queryNoDuplicates = new ArrayList<String>();
		
		for(int i = 0; i < query.size(); i ++){			
			boolean insert = true;

			for(int j = i+1; j < query.size(); j++){
					if (query.get(i).equalsIgnoreCase(query.get(j))) {
						insert = false;
					}
				}

			//TODO:
			//handle cases in which "or or", "and and" can be left in query
			if(insert || query.get(i).equalsIgnoreCase("OR") || query.get(i).equalsIgnoreCase("AND")) queryNoDuplicates.add(query.get(i));
		}		
		return queryNoDuplicates;
	}
		
	/**
	 * Returns one of the three 
	 * query types for a given query
	 * 
	 * @param query terms
	 * @return query type
	 */
	private String findQueryType(List<String> query){
		
		String type = "";
		
		if(!hasQuestionWords(query)){
			if(!hasStopWords(query)){
				type = KEYWORD_QUERY;
			}
			else type = STATEMENT_QUERY;
		}
		else {
			type = QUESTION_QUERY;
		}		
		return type;
	}
	
	/**
	 * Loads all schema fields used by Solr
	 */	
	private void loadQueryFields(){
		queryFields.add(FIELDS.id);
		queryFields.add(FIELDS.ji);
		queryFields.add(FIELDS.ui);
		queryFields.add(FIELDS.fm);
		queryFields.add(FIELDS.bm);
		queryFields.add(FIELDS.abs);
		queryFields.add(FIELDS.body);
		queryFields.add(FIELDS.journalTitle);
		queryFields.add(FIELDS.pmid);
		queryFields.add(FIELDS.pmc);
		queryFields.add(FIELDS.articleTitle);
		queryFields.add(FIELDS.author);
		queryFields.add(FIELDS.keywords);
//		queryFields.add(FIELDS.sections);
		queryFields.add(FIELDS.referenceTitle);
		queryFields.add(FIELDS.referenceId);
		queryFields.add(FIELDS.referenceAuthor);
		queryFields.add(FIELDS.year);			
	}
	
//
	
	/**
	 * Checks if a given query
	 * contains any question words
	 * 
	 * @param query terms
	 * @return true if question word is found
	 */
	private boolean hasQuestionWords(List<String> query){
		
		for(int i = 0; i < query.size(); i++){
			if(questionWordsList.contains(query.get(i))){
				return true;
			}
		}		
		return false;
	}
	
	/**
	 * Checks if a given query
	 * contains any stop words
	 * 
	 * @param query terms
	 * @return true if stopwords found
	 */
	private boolean hasStopWords(List<String> query){
		
		for(int i = 0; i < query.size(); i++){
			if(stopWordsList.contains(query.get(i))){
				return true;
			}
		}		
		return false;
	}
	
	/**
	 * Removes question words from query
	 * given the list of question words
	 * 
	 * @param query terms
	 * @return query with no question words
	 */	
	private List<String> removeQuestionWords(List<String> query){		
		
		List<String> resultQuery = new ArrayList<String>();		
		
		for(int i = 0; i < query.size(); i++){			

			if(!questionWordsList.contains(query.get(i))){
				resultQuery.add(query.get(i));
			}
		}		
		return resultQuery;
	}
	
	/**
	 * Removes stopwords from query
	 * given the list of stopwords
	 * 
	 * @param query terms
	 * @param task "extract" to get only stopwords found
	 * @return query with no stopwords
	 */	
	private List<String> handleStopWords(List<String> query, String task){		
		
		List<String> result = new ArrayList<String>();		
		
		for(int i = 0; i < query.size(); i++){			

			if(task.contains("extract")){
				if(stopWordsList.contains(query.get(i).toLowerCase())){
					result.add(query.get(i));
				}			
			}
			else if(!stopWordsList.contains(query.get(i).toLowerCase())){
				result.add(query.get(i));
			}
		}	
		return result;
		
	}


//	private HashMap<String,String> getFrequencyTerms(HashMap<String,String> bioMineQuery){
//    	StringBuilder freqTerms = new StringBuilder();
//    	StringBuilder inverseFreqTerms = new StringBuilder();
//
//    	List<String> thisQuery = Arrays.asList(bioMineQuery.get(QUERY_PARAM).split(" "));
//    	thisQuery = handleStopWords(thisQuery, "");
//
//    	//get all fields used in search
//    	List<String> qf = Arrays.asList(bioMineQuery.get(QUERY_FIELDS).split(" "));
//    	List<String> pf = Arrays.asList(bioMineQuery.get(PHRASE_FIELDS).split(" "));
//
//    	for(String queryWord : thisQuery){
//    		queryWord = queryWord.replace("\"", "");
//
//    		for(String field : qf){
//    			//removing boost factor
//    			if(field.contains("^")) field = field.substring(0, field.indexOf("^"));
//    			freqTerms.append(TF_PARAM+"(\""+field+"\",\""+queryWord+"\")");
//    		}
//    		for(String field : pf){
//    			//removing boost factor
//    			if(field.contains("^")) field = field.substring(0, field.indexOf("^"));
//    			inverseFreqTerms.append(IDF_PARAM+"(\""+field+"\",\""+queryWord+"\")");
//    		}
//    	}
//
//    	//retrieve lucene similarity score for the doc
//    	String frequencyTerms = "score";
//    	//retrieve TF for all terms in all search fields
//    	frequencyTerms += "," +	freqTerms.toString().replace(")"+TF_PARAM, "),"+TF_PARAM);
//    	//retrieve IDF for all terms in all search fields
//    	frequencyTerms += "," + inverseFreqTerms.toString().replace(")"+IDF_PARAM, "),"+IDF_PARAM);
//    	//retrieve content of all fields
//    	frequencyTerms +=",*";
//
//    	//put all display fields in query
//    	bioMineQuery.put(DISPLAY_FIELDS, frequencyTerms);

//		return bioMineQuery;
//	}


	/**
	 //	 * Reads stopwords list file
	 //	 */
//	private void loadStopWordsList(){
//
//		try{
//
//                    pathStopWords = biomineConfig.getProps().getProperty("queryStopwords", "queryStopwords");
//                    if(new File(pathStopWords).exists() == false){
//                        logger.error("Check that queryStopwords file exists in the your classpath or that you have set the 'queryStopwords' parameter in the properties");
//                        System.exit(0);
//                    }
//			BufferedReader reader = new BufferedReader(new FileReader(pathStopWords));
//			String line = null;
//
//			//loading stop-words list
//			while((line = reader.readLine()) != null){
//				stopWordsList = StrUtils.splitSmart(line,',');
//				line = reader.readLine();
//			}
//			reader.close();
//
//		}catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//	/**
//	 * Reads question words list file
//	 */
//	private void loadQuestionWordsList(){
//
//		try{
//                        pathQuestionWords = biomineConfig.getProps().getProperty("questionWords", "questionWords");
//                    if(new File(pathQuestionWords).exists() == false){
//                        logger.error("Check that questionWords file exists in you classpath or that you have set the 'questionWords' parameter in the properties");
//                        System.exit(0);
//                    }
//
//			BufferedReader reader = new BufferedReader(new FileReader(pathQuestionWords));
//			String line = null;
//
//			//loading question words list
//			while((line = reader.readLine()) != null){
//				questionWordsList = StrUtils.splitSmart(line,',');
//				line = reader.readLine();
//			}
//			reader.close();
//
//		}catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	

}

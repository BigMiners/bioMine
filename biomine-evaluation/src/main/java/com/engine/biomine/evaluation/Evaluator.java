package com.engine.biomine.evaluation;

import java.math.BigDecimal;
import java.util.*;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.engine.biomine.common.Configs;
import com.engine.biomine.common.FIELDS;
import com.engine.biomine.query.QueryManager;

/**
 *
 * @author halmeida
 *
 */
public class Evaluator {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
//	private boolean runEval;
	Properties props;
	private final String QUERY_PARAMS = "params";
	private final String DOC_SCORE = "score";
	private final String Q_NORMALIZ_VALUE = "queryNorm";
	private EntrezResponse response;
//	private GoldStandard goldStandard;
//	private final int retrieved_size;
	private final String pubmed = "PMID";
	private final String pmc = "PMC";


	public Evaluator(){
		Properties props = Configs.getInstance().getProps();
//		this.runEval = Boolean.parseBoolean(props.getProperty("run.eval"));
//		this.retrieved_size = Integer.parseInt(props.getProperty("docs.retrieved"));
		this.response = new EntrezResponse();
	}



	/**
	 * List only IDs retrieved for a query.
	 * Does not compute scores.
	 * @param results
     */
	public void outputIDResults(List<String> resultsPIDs){

		if (resultsPIDs != null) {

			StringBuilder sb = new StringBuilder();
			int outputSize = resultsPIDs.size();
//				retrieved_size;
//			if(resultsPIDs.size() < retrieved_size)
// 			outputSize = resultsPIDs.size();

			if (resultsPIDs.size() > 0) {
				for (int i = 0; i < outputSize; i++) {
					String PID = "";

					if (resultsPIDs.get(i) != null) {
						PID = resultsPIDs.get(i);
						int rank = i + 1;
						if(i < outputSize - 1) sb.append("("+rank+") " + PID + ", ");
						else sb.append("("+rank+") " + PID + "\n");
					}
				}
				logger.info(sb.toString());
			}
		} else logger.error("No results returned for query.");

	}

	/**
	 *
	 * @param scores
	 * @return
     */
	public Double computeMRRScore(List<Double> scores) {

		double total = 0.0;

//		if(runEval) {
			logger.info("Gold documents found -> " + scores.size());


			for (int i = 0; i < scores.size(); i++) {
				total += scores.get(i);
			}
			total = total / scores.size();

			logger.info("Mean reciprocal rank (MRR) -> " + total);
//		}
		return total;
	}

	public Double computeReciprocalRank(String userQuery, List<SolrDocument> results, List<String> goldIDs) {

		Double score = 0.0;

		// results from bioMine expanded query
		List<String> resultsBiomine = getPIDFromSolrDocs(results, userQuery);
//		outputIDResults(resultsBiomine);

//		if(runEval) {
//			List<String> goldDocPID = goldStandard.getGoldForQuery(userQuery);
			if((resultsBiomine == null) || (resultsBiomine.size() == 0))
				logger.info("bioMine  | NO results retrieved. ");
			else {

				for (int i = 0; i < resultsBiomine.size(); i++) {

					if (goldIDs.contains(resultsBiomine.get(i))) {
						double pos = i;
						pos = pos + 1;
						score = (1 / (pos));

						//get index of ID found among gold IDs
						int goldPosition = goldIDs.indexOf(resultsBiomine.get(i));
						//output correct ID from gold IDs list using index
						logger.info("bioMine | Gold document found -> " + goldIDs.get(goldPosition) + "\t Position: " + (pos) + "\t Score: " + score + "\n");

						break;
					} else if (i == resultsBiomine.size() - 1) {
						logger.info("bioMine | NO gold document found -> ");
						outputIDResults(goldIDs);
					}
				}
			}
//		}
		return score;
	}


	private void outputPMCResults(List<String> pmcids, int rcount){

		logger.info(" Entrez result count: "+rcount);
		int count = 1;

		for(int i=0; i< pmcids.size(); i++){

			logger.info(count + " Entrez result -> PMC: " + pmcids.get(i));
			count++;
		}
	}


	private List<String> getPIDFromSolrDocs(List<SolrDocument> results, String query){

		List<String> idlist = new ArrayList<String>();
		String id = "";

		for(SolrDocument doc : results){
			// FOR LYSO EVAL ONLY - REMOVE ASAP
			// If lyso query, give priority to PMIDs
//			if((query.toLowerCase().contains("lyso"))) {
//				if (doc.get(FIELDS.pmid) != null) {
//					id = "PMID" + doc.get(FIELDS.pmid).toString();
//				} else id = "PMC" + doc.get(FIELDS.pmc).toString();
//
//			}else {
				if (doc.get(FIELDS.pmc) != null) {
					id = "PMC" + doc.get(FIELDS.pmc).toString();
				} else id = "PMID" + doc.get(FIELDS.pmid).toString();
//			}

			idlist.add(id);
		}
		return idlist;
	}


	/**
	 * Inform query submitted to solr given a search
	 * @param header query response header
	 * @return query string
	 */
	private String getExpandedQuery(NamedList<Object> header){
		String query = header.get(QUERY_PARAMS).toString();
		query = query.substring(query.indexOf("q=")+2, query.indexOf(","));

		return query;
	}

	/**
	 * Inform the Solr score for a doc given a search
	 * @param doc sorl document
	 * @return document score
	 */
	private BigDecimal getScore(SolrDocument doc){
		return new BigDecimal(doc.get(DOC_SCORE).toString());
	}

	/**
	 * Inform the ID field indexed for a given Solr document
	 * @param doc solr document
	 * @return doc ID
	 */
	private String getDocId(SolrDocument doc){
		return doc.get(FIELDS.id).toString();
	}




//	public void compareRanking(String userQuery, List<String> goldIDs){
//
////		if(runEval) {
////			List<String> goldDocPID = goldStandard.getGoldForQuery(userQuery);
//		String[] sourceToCompare = props.getProperty("compare.ranking").split(",");
//
//		for (int j = 0; j < sourceToCompare.length; j++) {
//
//			String thisSource = sourceToCompare[j];
//			if (!thisSource.contains("false")) {
//
//				List<String> toCompare = null;
//				switch (thisSource) {
//					case "pubmed":
//						thisSource = "PubMed";
//						toCompare = response.getPmidsFromResults(userQuery);
//						break;
//					case "pmc":
//						thisSource = "PMC";
//						toCompare = response.getPmcidFromResults(userQuery);
//						break;
//					case "solr":
//						thisSource = "Solr";
////							toCompare = getPIDFromSolrDocs(getbioMinePmcIds(userQuery));
//						break;
//				}
//
//				if ((toCompare == null) || (toCompare.size() == 0))
//					logger.info(thisSource + " | NO results retrieved. ");
//				else {
//					for (int i = 0; i < toCompare.size(); i++) {
//						if (goldIDs.contains(toCompare.get(i))) {
//							//get index of ID found among gold IDs
//							int goldPosition = goldIDs.indexOf(toCompare.get(i));
//							int pos = i + 1;
//							logger.info(thisSource + " | Gold document found -> " + goldIDs.get(goldPosition) + "\t Position: " + pos);
//							break;
//
//						} else if (i == toCompare.size() - 1) {
//							logger.info(thisSource + " | NO gold document found. ");
////						outputIDResults(goldDocPID);
//						}
//					}
//				}
//			}
//		}
////		}
//	}

	/**
	 * Queries Solr with not expanded query
	 * and retrieves list of result documents
	 * @param userQuery raw user query
	 * @return list of Solr document results
	 */
//	private List<SolrDocument> getbioMinePmcIds(String userQuery){
//		String task = "eval";
//		QueryManager qm = new QueryManager(conf);
//		List<SolrDocument>results = qm.getResultsForQuery(userQuery, task);
//
//		return results;
//	}

	/**
	 * Inform the query normalization score
	 * it's a constant given a search
	 * (debugQuery has to be enabled)
	 *
	 * @param explainMap search explainMap
	 * @return query normalization score
	 */
//	private BigDecimal getQueryNorm(Map<String, String> explainMap){
//
//		String key = "";
//		Entry<String,String> entry = explainMap.entrySet().iterator().next();
//		if(entry != null) key = entry.getKey();
//
//		Map<String,String> instanceInfo = getExplainMapInstance(explainMap, key);
//
//		for(String info : instanceInfo.keySet()){
//				if(info.contains(Q_NORMALIZ_VALUE)){
//				return new BigDecimal(instanceInfo.get(info).trim());
//			}
//		}
//
//		return new BigDecimal(0);
//	}



//	/**
//	 * Retrieves information for a Solr ExplainMap instance
//	 * (contained in a search result)
//	 * @param explainMap map with explain values
//	 * @param key document ID in explainMap
//	 * @return explain map of {key,values} for a document
//	 */
//	private Map<String,String> getExplainMapInstance(Map<String, String> explainMap, String key){
//
//		Map<String,String> debugInstance = new HashMap<String,String>();
//
//		for(Entry<String,String> entry: explainMap.entrySet()){
//			if(entry.getKey().equalsIgnoreCase(key)){
//				String[] lines = entry.getValue().split("\n");
//				for(String line : lines){
//					if(line.length()>1){
//						String[] oneLine = line.split("=");
//						debugInstance.put(oneLine[1], oneLine[0]);
//					}
//				}
//			}
//		}
//
//		return debugInstance;
//	}


	
	

}

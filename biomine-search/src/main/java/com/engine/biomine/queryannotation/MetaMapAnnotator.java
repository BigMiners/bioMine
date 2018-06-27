package com.engine.biomine.queryannotation;

/* //<!-- Silence Metamap -->
import gov.nih.nlm.nls.metamap.Ev;
import gov.nih.nlm.nls.metamap.Mapping;
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;
//<!-- Silence Metamap --> */
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Annotates a query with UMLS concepts
 * provided by the MetaMap system
 * 
 * @author halmeida
 *
 */
public class MetaMapAnnotator {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final String TERM_PROCESSING = "-z";
	 //<!-- Silence Metamap -->
	/* private final MetaMapApi API;*/
	// <!-- Silence Metamap -->
		
	public MetaMapAnnotator(){
		// <!-- Silence Metamap -->
		/* API = new MetaMapApiImpl();
		API.setOptions(TERM_PROCESSING); */
		// <!-- Silence Metamap -->
	}
	
		
	/**
	 * Lists UMLS concepts found in user  
	 * query using the MetaMap system
	 *  
	 * @param userQuery string provided by user
	 * @return map of {concept ID, concept info}
	 */
	private HashMap<String,String[]> getMetaMapConcepts(String userQuery){
		// <!-- Silence Metamap -->
		/* HashMap<String,String[]> annotations = new HashMap<String,String[]>();
		List<Result> resultList = null;
		
		try{
			resultList = API.processCitationsFromString(userQuery);
		}catch (RuntimeException e){
			logger.error("MetaMap server exception: could not retrieve MetaMap annotations", e);
		}
		
		Iterator<Result> iter = resultList.iterator();
		while(iter.hasNext()){
			Result item = iter.next();			

			try {
				for(Utterance ut : item.getUtteranceList()){
					//PCM : phrase, candidate, mappings
					for(PCM pcm : ut.getPCMList()){					
						for(Mapping map : pcm.getMappingList()){						
							//Ev: Evaluation instance							
							for(Ev mapEv: map.getEvList()){
								String matched = mapEv.getMatchedWords().toString();
								matched = matched.replace("[", "");	
								matched = matched.replace("]", "");
								matched = matched.replace(",", "");
								String[] conceptData = new String[]{
										matched,								//actual term searched
										mapEv.getConceptName(),					//UMLS concept
										mapEv.getPreferredName(),				//UMLS preferred name
										Integer.toString(mapEv.getScore()),		//MetaMap negated normalized score
										mapEv.getSemanticTypes().toString()		//UMLS semantic type
								};								
								annotations.put(mapEv.getConceptId(), conceptData);							
							}
						}
					}
				}
			} catch (Exception e) {				
				e.printStackTrace();
			}
		}		

		return annotations; */
		// <!-- Silence Metamap -->
                return null;
		// <!-- Silence Metamap -->
	}

	/**
	 * Adds UMLS concepts extracted using MetaMap 
	 * to a user query, removing duplicates
	 * 
	 * @param userQuery string provided by user
	 * @return userQuery query with UMLS concepts found 
	 */
	public String addMetaMapConcepts(String userQuery){
		HashMap<String,String[]> metaMapAnnotations = getMetaMapConcepts(userQuery);
		HashMap<String,String> tempConcepts = new HashMap<>();
		String normUserQuery = normalizeText(userQuery);

		if(!metaMapAnnotations.isEmpty()){
			for (String entry : metaMapAnnotations.keySet()){

				String queryTerm = metaMapAnnotations.get(entry)[0];
				String conceptName = metaMapAnnotations.get(entry)[1];
				String preferredName = metaMapAnnotations.get(entry)[2];

				isCharConsistent(queryTerm, conceptName);
				isCharConsistent(queryTerm, preferredName);
				//normalized annotations to facilitate check against query terms
				String queryTermNorm = normalizeText(queryTerm);
				String conceptNorm = normalizeText(conceptName);
				String preferredNorm = normalizeText(preferredName);

				String[] conceptTerms = conceptNorm.split(" ");
				String[] preferredTerms = preferredNorm.split(" ");
				boolean add = true;

				//add concepts to query and search for collocation
				//avoid *very* noisy repetitions: only add annotation if query does not have ANY term
				if(!normUserQuery.contains(conceptNorm) && !tempConcepts.keySet().contains(conceptName)){
					for(String annotationTerm : conceptTerms){
						if(annotationTerm.length() > 1 && isValidTerm(queryTerm, conceptName, annotationTerm)){
							tempConcepts.put(conceptName, queryTerm);
						}
					}
				}

				if(!isLemma(conceptNorm, preferredNorm)){
					for(String annotationTerm : preferredTerms){
						if(annotationTerm.length()> 1 && isValidTerm(queryTerm, preferredName, annotationTerm)){
							tempConcepts.put(preferredName, queryTerm);
						}
					}
				}
			}

			//remove extra spaces left after cleaning word duplicates
			userQuery = userQuery.replaceAll("  ", " ");

			Iterator<String> iter = tempConcepts.keySet().iterator();
			while(iter.hasNext()){
				String annotation = iter.next();
				String queryWord = tempConcepts.get(annotation);
				String wordOR = "("+ queryWord + " OR ";
				String newWordOR = wordOR + annotation + " OR ";

				if(userQuery.contains(wordOR)) userQuery = userQuery.replace(wordOR, newWordOR);
				else userQuery = userQuery.replace(queryWord, wordOR + annotation +")");
			}
		}

		return userQuery;
	}


	private boolean isValidTerm(String queryTerm, String annotation, String annotationTerm){
		annotation = annotation.toLowerCase();
		queryTerm = queryTerm.toLowerCase();

		if(!annotation.contains(queryTerm) && !queryTerm.contains(annotation)) {
			if (isCharConsistent(queryTerm, annotation)) {
				if (!isLemma(queryTerm, annotation)) {
					if(!isNoisyWord(queryTerm))
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Check if a term lemma (substring)
	 * is within another string
	 * @param one string (lemma) to check
	 * @param two
     * @return
     */
	private boolean isLemma(String one, String two){
		String firstPartialOne = one.substring(0, (one.length()/2));
		String secondPartialOne = one.substring((one.length()/2), one.length());

		if (two.contains(firstPartialOne) || two.contains(secondPartialOne))
			return true;
		else
			return false;
	}


	/**
	 * Check consistent of annotation (avoids noise)
	 * Query term + annotation must match same pattern.
	 *
	 * @param term
	 * @param annotation
     * @return
     */
	private boolean isCharConsistent(String term, String annotation){
		if(StringUtils.isAlphanumericSpace(term)){
				if(StringUtils.isAlphaSpace(term)){
					if(StringUtils.isAlphaSpace(annotation)) return true;
				} else if(StringUtils.isAlphanumericSpace(annotation)) return true;
		}

		return false;
	}

	/**
	 * Normalize concepts that make harder to check
	 * for duplicates between query terms and annotation terms
	 * @param text
	 * @return
	 */
	private String normalizeText(String text){

		text = text.toLowerCase();
		text = text.replace("-"," ");
		text = text.replace(","," ");
		text = text.replace("+","");
//		wildcard!
//		text = text.replace("*","");

		return text;
	}

	private boolean isNoisyWord(String term){
		if(term.contains("treatment") ||
				term.contains("high") ||
//				term.contains("exam") ||
				term.contains("life") ||
//				term.contains("low") ||
//				term.contains("numerous") ||
				term.contains("well")
				)
			return true;
		return false;
	}

}

package com.engine.biomine.evaluation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class EntrezResponse {
	
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final String exprXpathCount = "//eSearchResult/Count";
	private final String exprXpathID = "//IdList/Id";
	private final String urlPMCEntrez = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pmc&sort=relevance&term=";
	private final String urlPMIDEntrez = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&sort=relevance&term=";
	private final String pubmed = "PMID";
	private final String pmc = "PMC";

	/**
	 * Fetch PMCIDs for a given user query
	 * @param query
	 * @return
	 */
	public List<String> getPmcidFromResults(String query) {
		query = getURLQuery(query);
		query = getOAURLQuery(query);

		Document doc = getResponseForQuery(urlPMCEntrez + query);
		return getIdsFromResponse(doc, pmc);
	}

	/**
	 * Fetch PMIDs for a given user query
	 * @param query
	 * @return
	 */
	public List<String> getPmidsFromResults(String query) {
		query = getURLQuery(query);

		Document doc = getResponseForQuery(urlPMIDEntrez + query);
		return getIdsFromResponse(doc, pubmed);
	}

	/**
	 * Fetch PMC number of results for a given user query
	 * @param query
	 * @return
     */
	public int getPmcResultCount(String query){
		query = getURLQuery(query);

		Document doc = getResponseForQuery(urlPMCEntrez + query);
		return getResultCount(doc);
	}

	/**
	 * Fetch PMID number of results for a given user query
	 * @param query
	 * @return
     */
	public int getPmidResultCount(String query){

		Document doc = getResponseForQuery(urlPMIDEntrez + query);
		return getResultCount(doc);
	}


	/**
	 * Not used in evaluator
	 *  Used only at mycoclapEntry class...
	 * @param IDs
	 * @return
	 *
	 */
	public List<Document> getPmcDocResults(List<String> IDs) {	    
		
		List<Document> pmcDocs = new ArrayList<Document>();
		
		for(String id : IDs){
			pmcDocs.add(getDocFromResponse(id));
		}
		
		return pmcDocs;
	}
	

	
	/**
	 * Retrieves a response XML doc for an Entrez URL request
	 * @param url user query
	 * @return XML document response
	 */	
	private Document getResponseForQuery(String url){

		URL thisURL;
		Document doc = null;
		try {
			thisURL = new URL(url);
			doc = getParsedDocument(thisURL);
			
		} catch (IOException e) {		
			e.printStackTrace();
		}		
		return doc;		
	}	
	
	/**
	 * Lists the Ids (PMC or PMID) from a result XML doc
	 * @param XMLdoc result of an Entrez search
	 * @param db database used: PubMed / PMC
	 * @return list of PMCIDs found
	 */	
	private List<String> getIdsFromResponse(Document XMLdoc, String db){
	 	   		
		NodeList nodes;
		ArrayList<String> data = new ArrayList<String>();
		XPath xpathExecutor = XPathFactory.newInstance().newXPath();
	
		try {
            nodes = (NodeList) xpathExecutor.evaluate(exprXpathID, XMLdoc, XPathConstants.NODESET);            
            for (int i = 0; i < nodes.getLength(); i++) {
                Node currentNode = nodes.item(i);
				String entry = db+currentNode.getTextContent();
                data.add(entry);
            }
        } catch (XPathExpressionException ex) {
            logger.error("XPathExpressionException: could not apply xpath expression", ex);
        }
        	
		return data;	
	}

	/**
	 * Retrieves the results count from a result XML doc
	 * @param XMLdoc result of an Entrez search
	 * @return number of results for entrez search
     */
	private int getResultCount(Document XMLdoc){
		NodeList nodes;
		int count = 0;
		XPath xpathExecutor = XPathFactory.newInstance().newXPath();
		
		try {
            nodes = (NodeList) xpathExecutor.evaluate(exprXpathCount, XMLdoc, XPathConstants.NODESET);            
            for (int i = 0; i < nodes.getLength(); i++) {
                Node currentNode = nodes.item(i);
                count = Integer.parseInt(currentNode.getTextContent());                
            }
        } catch (XPathExpressionException ex) {
            logger.error("XPathExpressionException: could not apply xpath expression", ex);
        }
		return count;
	}
	
	/**
	 * Retrieve the XML parsing of a response XML doc
	 * @param url entrez url request
	 * @return xml document response
	 */
	private Document getParsedDocument(URL url){		
				   		
		DocumentBuilder builder;
        Document doc = null;         
        		
        try {
        	InputStream inputs = url.openStream();   
        	
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);            

            builder = factory.newDocumentBuilder();
            doc = builder.parse(inputs); 
            
            inputs.close();
            
        } catch (ParserConfigurationException ex) {
            logger.error("ParserConfigurationException: could not parse xml document", ex);
        } catch (SAXException ex) {
            logger.error("SAXException: could not parse xml document", ex);
        } catch (IOException ex) {
            logger.error("IOException: could not parse xml document", ex);
        }
        
        return doc;
	}
	
		
	/**
	 * Edits raw query for URL submitted to PMC OA only
	 * @param query user query
	 * @return URL formatted PMC OA query
	 */
	private String getURLQuery(String query){
		query = query.replace(" ", "+");
		query = query.replace("/", "%2F");
		query = query.replace("[", "%5B");
		query = query.replace("]", "%5D");
		query = query.replace("-", "%2D");

		return query;
	}

	private String getOAURLQuery(String query){
		return "open+access[filter]+" + query;
	}
	
	/**
	 * Retrieve XML parsing of an article given a PMCID. 
	 * @param ID  pmcid
	 * @return list of w3c documents
	 */	
	private Document getDocFromResponse(String ID){
		Document doc = null;
//		DocumentBuilder builder;
		URL url;
		String query = ID;

		try {
			url = new URL("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pmc&id=" + query + "&retmode=xml");
			doc = getParsedDocument(url);
//			System.out.println("Entrez URL: "+ url.toString());

		} catch (IOException ex) {
            logger.error("IOException: could not parse xml document", ex);
        }
        
        return doc;		
		
	}

}

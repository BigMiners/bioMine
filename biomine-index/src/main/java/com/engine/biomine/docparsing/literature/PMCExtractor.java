package com.engine.biomine.docparsing.literature;


import com.engine.biomine.common.doc.Author;
import com.engine.biomine.common.doc.LiteratureDoc;
import com.engine.biomine.common.doc.Reference;
import com.google.common.collect.Lists;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author ludovic
 */
public class PMCExtractor extends DocumentExtractor{

    private final String exprXpathAbstract = "//abstract";
    private final String exprXpathBody = "//body";
    private final String exprXpathTitle = "//journal-meta/journal-title-group/journal-title";
    private final String exprXpathPmid = "//article-id[@pub-id-type='pmid']";
    private final String exprXpathPmc = "//article-id[@pub-id-type='pmc']";
    private final String exprXpathArticleTitle = "//article-title";
    private final String exprXpathAuthor = "//contrib[@contrib-type='author']/name";
    private final String exprXpathKeywords = "//kwd-group/kwd";
    private final String exprXpathSectionTitle = "//sec/title";
    private final String exprXpathRef = "//ref/mixed-citation|//ref/element-citation";
    private final String exprXpathYearP = "//pub-date[@pub-type='ppub']/year";
    private final String exprXpathYearE = "//pub-date[@pub-type='epub']/year";
    private final String exprXpathFigCaption = "//floats-group";

    
    @Override
    public LiteratureDoc extractDocValues(Document document) {
        LiteratureDoc doc = new LiteratureDoc();

        //do parsing of the doc to retrieve the different parts of the article, abstract, body, etc.
        if (document != null) {

            //process journal metadata
            String journalTitle = getTagContent(document, exprXpathTitle);
            if (journalTitle != null && journalTitle.length() > 0) {
                doc.setJournalTitle(journalTitle.trim());
            }
            
            //pictures, tables (etc) captions
            String captions = getFloatsGroupTagContent(document, exprXpathFigCaption);
            if(captions != null && !captions.isEmpty()){
            	doc.setCaptions(captions);
            }
            
            //pmid
            String pmid = getTagContent(document, exprXpathPmid);
            if (pmid != null && pmid.length() > 0) {
                pmid = idNormalizer(pmid);
                doc.setPmid(pmid.trim());                
            }

            //pmc
            String pmc = getTagContent(document, exprXpathPmc);
            if (pmc != null && pmc.length() > 0) {
                pmc = idNormalizer(pmc);
                doc.setPmc(pmc.trim());
            }

            //process authors
            Author[] authors = getAuthors(document, exprXpathAuthor);
            if (authors != null && authors.length > 0) {
                doc.setAuthors(authors);
            }

            //article title
            String articleTitle = getTagContent(document, exprXpathArticleTitle);
            if (articleTitle != null && articleTitle.length() > 0) {
                doc.setTitle(articleTitle.trim());
            }

            String abstractText = getTagContent(document, exprXpathAbstract);
            if (abstractText != null && abstractText.length() > 0) {
                //add abstract content to solr doc
                doc.setAbs(abstractText);
            }

            String[] keywords = getTagList(document, exprXpathKeywords);
            if (keywords != null && keywords.length > 0) {
                //add abstract content to solr doc
                doc.setKeywords(keywords);
            }

            //body section
            String bodyText = getBodyTagContent(document, exprXpathBody);
            doc.setBody(bodyText);

            //article year
            String yearStr = getTagContent(document, exprXpathYearP);      
            if(String.valueOf(yearStr).isEmpty()){
            	yearStr = getTagContent(document, exprXpathYearE);
            }
            int year = getPublicationDate(document, yearStr);
            doc.setYear(year);

            Reference[] refs = getReferences(document, exprXpathRef);
            doc.setReferences(refs);   

            xpathExecutor.reset();
        }
        return doc;
    }


    private String getFloatsGroupTagContent(Document document, String expathExpr){
    	
    	Node currentNode;
        StringBuilder out = new StringBuilder();
        
        try {
        	currentNode = (Node) xpathExecutor.evaluate(expathExpr, document, XPathConstants.NODE);
        	if (currentNode != null && currentNode.hasChildNodes()) {
        		for (int i = 0; i < currentNode.getChildNodes().getLength(); i++) {
        			//list all under floats-group
        			NodeList children = currentNode.getChildNodes().item(i).getChildNodes();
        			
        			for (int k = 0; k < children.getLength(); k++) {
        				Node subChild = children.item(k);
        			
        				//get all under caption
        				if(subChild.getNodeName().equalsIgnoreCase("caption")){
        					NodeList contentSubChild = subChild.getChildNodes();
        					
        					for(int j = 0; j < contentSubChild.getLength(); j++){
        						//get all content under p
        						if(contentSubChild.item(j).getNodeName().equalsIgnoreCase("p")){
        							out.append(contentSubChild.item(j).getTextContent() + "\n");
        						}
        					}        					
        					
        				}        				
        			}
        		}
        	}
        } catch (XPathExpressionException ex) {
            logger.error("XPathExpressionException: could not apply xpath expression", ex);
        }
        
        return out.toString();
    }
    
    private String getBodyTagContent(Document document, String expathExpr) {
        Node currentNode;
        StringBuilder out = new StringBuilder();
        try {
            currentNode = (Node) xpathExecutor.evaluate(expathExpr, document, XPathConstants.NODE);
            if (currentNode != null && currentNode.hasChildNodes()) {
                for (int i = 0; i < currentNode.getChildNodes().getLength(); i++) {
                    Node child = currentNode.getChildNodes().item(i);
                    NodeList subChild = child.getChildNodes();
                    for (int k = 0; k < subChild.getLength(); k++) {
                        Node kNode = subChild.item(k);
                        out.append(kNode.getTextContent() + "\n");
                    }
                }
            }
        } catch (XPathExpressionException ex) {
            logger.error("XPathExpressionException: could not apply xpath expression", ex);
        }

        return out.toString();
    }

    private int getPublicationDate(Document document, String yearStr) {
        int year = 0;
        try {
            year = Integer.parseInt(yearStr.replaceAll("\\s", ""));
        } catch (Exception e) {        	

        }
        return year;    
    }

}


package com.engine.biomine.docparsing.literature;

import com.engine.biomine.common.doc.Author;
import com.engine.biomine.common.IOUtil;
import com.engine.biomine.common.doc.LiteratureDoc;
import com.google.common.collect.Lists;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
public class PubMedExtractor extends DocumentExtractor{

    private final String exprXpathMedline = "//MedlineCitationSet/MedlineCitation";
    private final String exprXpathAbstract = "//Abstract";
    private final String exprXpathTitle = "//Journal/Title";
    private final String exprXpathPmid = "//PMID";
    private final String exprXpathPmc = "//ArticleId[@IdType='pmc']";
    private final String exprXpathArticleTitle = "//ArticleTitle";
    private final String exprXpathAuthor = "//Author[@ValidYN='Y']";
    private final String exprXpathMesh = "//MeshHeadingList/MeshHeading";



    public PubMedExtractor(){
        super();
    }

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

            //pmid
            String pmid = getTagContent(document, exprXpathPmid);
            if (pmid != null && pmid.length() > 0) {
                doc.setPmid(pmid.trim());                
            }

            //pmc
            String pmc = getTagContent(document, exprXpathPmc);
            if (pmc != null && pmc.length() > 0) {
                doc.setPmc(pmc.trim());
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

            Author[] authors = getAuthors(document, exprXpathAuthor);
            if (authors != null && authors.length > 0) {
                doc.setAuthors(authors);
            }

            String[] meshTerms = getTagList(document, exprXpathMesh);
            if (meshTerms != null && meshTerms.length > 0){
                doc.setMeshTerms(meshTerms);
            }
//
//                        Reference[] refs = getReferences(doc, exprXpathRef);
//                        doc.setReferences(refs);

            xpathExecutor.reset();
        }

        return doc;
    }

    public long getArticleCount(File inputFile) {
        long count = 0;
        InputStream fileStream = IOUtil.getINSTANCE().getFileStream(inputFile);
        NodeList medlineCitationSet = null;
        try {
            //parse original xml file
            Document doc = getParsedDocument(fileStream);
            fileStream.close();
            medlineCitationSet = (NodeList) xpathExecutor.evaluate(exprXpathMedline, doc, XPathConstants.NODESET);
            if (medlineCitationSet != null) {
                count = medlineCitationSet.getLength();
            }
        } catch (XPathExpressionException ex) {
            logger.error("XPathExpressionException: could not apply xpath expression", ex);
        } catch (IOException ex) {
            logger.error("IOException: could not read xml doc", ex);
        }

        return count;
    }


    private String[] getTagList(Document document, String expathExpr) {
        ArrayList<String> out = Lists.newArrayList();
        NodeList nodes;
        try {
            nodes = (NodeList) xpathExecutor.evaluate(expathExpr, document, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node currentNode = nodes.item(i);
                out.add(currentNode.getTextContent());
            }
        } catch (XPathExpressionException ex) {
            logger.error("XPathExpressionException: could not apply xpath expression", ex);
        }

        return out.toArray(new String[0]);
    }


}

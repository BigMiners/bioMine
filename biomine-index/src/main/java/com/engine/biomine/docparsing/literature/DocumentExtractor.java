package com.engine.biomine.docparsing.literature;

import com.engine.biomine.common.doc.*;
import com.engine.biomine.common.IOUtil;
import com.google.common.collect.Lists;
import java.io.BufferedInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.InputSource;

/**
 * Created by halmeida on 7/15/16.
 */
public abstract class DocumentExtractor {

    final Logger logger = LoggerFactory.getLogger(this.getClass());
    XPath xpathExecutor;
    private final String exprXpathMedline = "//MedlineCitationSet/MedlineCitation";
    //reference (tag) to identify single entry in a file with multiple documents
    String documentHead;
    private final String documentSplitTag = "MedlineCitationSet";
    private int docCount = 0;

    public DocumentExtractor() {
        xpathExecutor = XPathFactory.newInstance().newXPath();
        documentHead = exprXpathMedline;
    }

    public BiomineDoc extractDocValues(InputStream inputstream) {
        Document doc = getParsedDocument(inputstream);
        return extractDocValues(doc);
    }

    public ArrayList<BiomineDoc> extractValuesFromDocs(InputStream inputStream) {
        ArrayList<BiomineDoc> docs = this.splitXmlAndParse(inputStream);
        return docs;
    }

    public abstract BiomineDoc extractDocValues(Document doc);

    public Document getParsedDocument(InputStream inputStream) {
        DocumentBuilder builder;
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            builder = factory.newDocumentBuilder();
            document = builder.parse(inputStream);

        } catch (ParserConfigurationException ex) {
            logger.error("ParserConfigurationException: could not parse xml doc", ex);
        } catch (SAXException ex) {
            logger.error("SAXException: could not parse xml doc", ex);
        } catch (IOException ex) {
            logger.error("IOException: could not parse xml doc", ex);
        }

        return document;
    }

    public ArrayList<BiomineDoc> extractDocValues(File inputFile) {
        ArrayList<BiomineDoc> result = new ArrayList<>();
        InputStream fileStream = null;

        try {
            fileStream = IOUtil.getINSTANCE().getFileStream(inputFile);
            result = splitXmlAndParse(fileStream);
            fileStream.close();

        } catch (IOException e) {
            logger.error("IOException: could not extract values from xml doc", e);
        }

        return result;
    }

    private Document convertStringToDocument(String xmlNode) {
        Document document = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);

        DocumentBuilder builder;
        try {
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            builder = factory.newDocumentBuilder();
            document = builder.parse(new InputSource(new StringReader(xmlNode)));

        } catch (ParserConfigurationException ex) {
            logger.error("ParserConfigurationException: could not parse xml doc", ex);
        } catch (SAXException ex) {
            logger.error("SAXException: could not parse xml doc", ex);
        } catch (IOException ex) {
            logger.error("IOException: could not parse xml doc", ex);
        }

        return document;
    }

    /**
     * This method parses the content of a xml file and applies a set of xpath
     * expressions to produce a list of BiomineDoc
     *
     * @param fileStream
     * @return
     */
    private ArrayList<BiomineDoc> splitXmlAndParse(InputStream fileStream) {
        ArrayList<BiomineDoc> documentList = Lists.newArrayList();
        
        //parse the xml file with a stax parser
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileStream);
        bufferedInputStream.mark(1);
        
        try {
            XMLStreamReader xstreamReader = xmlInputFactory.createXMLStreamReader(bufferedInputStream);            
            if (xstreamReader.getEventType() == XMLStreamReader.START_DOCUMENT) {
                //advance to one tag
                xstreamReader.next();
            }
            
            if (xstreamReader.getEventType() == XMLStreamReader.DTD) {
                //advance to one tag
                xstreamReader.next();
            }            

            if (documentSplitTag.equals(xstreamReader.getLocalName())) {
                //parse multiple nested xml files
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();
                while (xstreamReader.nextTag() == XMLStreamConstants.START_ELEMENT) {
                    StAXSource source = new StAXSource(xstreamReader);
                    StringWriter outWriter = new StringWriter();
                    transformer.transform(source, new StreamResult(outWriter));
                    if (outWriter.toString().trim().length() > 0) {
                        Document document = this.convertStringToDocument(outWriter.toString());
                        //apply xpath expression to create BiomineDoc
                        BiomineDoc indexDoc = extractDocValues(document);
                        documentList.add(indexDoc);
                        //increment number of docs in the file
                        this.docCount += 1;
                    }
                }
            } else {
                //reset the fileStream to the start of the file
                bufferedInputStream.reset();

                Document document = this.getParsedDocument(bufferedInputStream);
                if (document != null) {
                    //apply xpath expression to create BiomineDoc
                    BiomineDoc indexDoc = extractDocValues(document);
                    documentList.add(indexDoc);
                    //increment number of docs in the file
                    this.docCount += 1;
                }
            }

        } catch (XMLStreamException ex) {
            logger.warn("XMLStreamException: parsing error", ex);
        } catch (TransformerConfigurationException ex) {
            logger.error("TransformerConfigurationException: Failed to transform xml file", ex);
        } catch (TransformerException ex) {
            logger.error("TransformerException: Failed to transform xml file", ex);
        } catch (IOException ex) {
            logger.error("IOException: failed to process literature file: ", ex);
        }

        return documentList;
    }

    protected String getTagContent(Document document, String expathExpr) {
        Node currentNode;
        StringBuilder out = new StringBuilder();
        try {
            currentNode = (Node) xpathExecutor.evaluate(expathExpr, document, XPathConstants.NODE);
            if (currentNode != null && currentNode.hasChildNodes()) {
                for (int i = 0; i < currentNode.getChildNodes().getLength(); i++) {
                    Node child = currentNode.getChildNodes().item(i);
                    out.append(child.getTextContent());
                }
            }
        } catch (XPathExpressionException ex) {
            logger.error("XPathExpressionException: could not apply xpath expression", ex);
        }

        return out.toString();
    }

    protected Author[] getAuthors(Document document, String expathExpr) {
        ArrayList<Author> authors = Lists.newArrayList();
        NodeList nodes;
        try {
            nodes = (NodeList) xpathExecutor.evaluate(expathExpr, document, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node currentNode = nodes.item(i);

                if (currentNode != null && currentNode.hasChildNodes()) {
                    String name = "";
                    String surname = "";
                    for (int j = 0; j < currentNode.getChildNodes().getLength(); j++) {
                        Node child = currentNode.getChildNodes().item(j);
                        if (child.getNodeName().equals("LastName")) {
                            surname = child.getTextContent();
                        }

                        if (child.getNodeName().equals("ForeName")) {
                            name = child.getTextContent();
                        }
                    }
                    //add author name and surname to authors
                    authors.add(new Author(name, surname));
                }
            }
        } catch (XPathExpressionException ex) {
            logger.error("XPathExpressionException: could not apply xpath expression", ex);
        }
        return authors.toArray(new Author[0]);
    }

    protected Author extractAuthor(Node node) {
        String name = "";
        String surname = "";
        if (node.getNodeName().equals("name")) {
            for (int k = 0; k < node.getChildNodes().getLength(); k++) {
                Node authorNode = node.getChildNodes().item(k);
                if (authorNode.getNodeName().equals("surname")) {
                    surname = authorNode.getTextContent();
                }

                if (authorNode.getNodeName().equals("given-names")) {
                    name = authorNode.getTextContent();
                }
            }
        }
        //add author name and surname to authors
        return (new Author(name, surname));
    }

    protected Reference[] getReferences(Document document, String expathExpr) {
        ArrayList<Reference> refs = Lists.newArrayList();
        NodeList nodes;
        try {
            nodes = (NodeList) xpathExecutor.evaluate(expathExpr, document, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                Reference currentRef = new Reference();
                ArrayList<Author> refAuthors = Lists.newArrayList();
                String refTitle;
                int refYear;
                String refSource;
                //iterate through the tags of each ref
                Node currentNode = nodes.item(i);
                if (currentNode != null && currentNode.hasChildNodes()) {
                    for (int j = 0; j < currentNode.getChildNodes().getLength(); j++) {
                        Node child = currentNode.getChildNodes().item(j);
                        if (child.getNodeName().equals("person-group")) {
                            NodeList subChildList = child.getChildNodes();
                            for (int c = 0; c < subChildList.getLength(); c++) {
                                Author author = extractAuthor(subChildList.item(c));
                                //add author name and surname to authors
                                refAuthors.add(author);
                            }
                        }
                        if (child.getNodeName().equals("name")) {
                            Author author = extractAuthor(child);
                            //add author name and surname to authors
                            refAuthors.add(author);
                        }

                        if (child.getNodeName().equals("year")) {
                            String yearStr = child.getTextContent();
                            if (yearStr.length() > 4) {
                                yearStr = yearStr.substring(0, 4);
                            }
                            if (yearStr.matches("[0-9]+")) {
                                refYear = Integer.parseInt(yearStr);
                                currentRef.setYear(refYear);
                            } else {
                                currentRef.setYear(0);
                            }
                        }

                        if (child.getNodeName().equals("article-title")) {
                            refTitle = child.getTextContent();
                            currentRef.setTitle(refTitle);
                        }

                        if (child.getNodeName().equals("source")) {
                            refSource = child.getTextContent();
                            currentRef.setSource(refSource);
                        }

                        if (child.getNodeName().equals("pub-id")) {
                            Node pubId = child.getAttributes().getNamedItem("pub-id-type");
                            if (pubId.getNodeValue().equals("pmid")) {
                                String refId = child.getTextContent();
                                refId = refId.trim();
                                if (refId.length() > 10) {
//                                    logger.warn("Reference ID: " + refId + " in article "
//                                            + doc.getElementsByTagName("article-id").item(0).getTextContent() + " has > than 10 chars.");
                                    refId = "0";
                                }
                                currentRef.setPubId(refId);
                            }
                        }

                    }
                    currentRef.setAuthors(refAuthors);
                }
                refs.add(currentRef);
            }
        } catch (XPathExpressionException ex) {
            logger.error("XPathExpressionException: could not apply xpath expression", ex);
        }
        return refs.toArray(new Reference[0]);
    }

    public String getDocumentHead() {
        return documentHead;
    }

    public void setDocumentHead(String documentHead) {
        this.documentHead = documentHead;
    }

    public int getDocCount() {
        return docCount;
    }

}

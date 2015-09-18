package com.engine.biomine.indexing;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IndexManager {
	
	private final Configs conf;
    private final String serverUrl;
    private final SolrClient solrClient;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    //commit after 3 sec
    private final int commitWithinMs = 10;
    private final ArrayList<String> extensionLst;
    private final XmlMapper mapper;

    public IndexManager(Configs config) {
        this.conf = config;
        String serverPath = config.getProps().getProperty("server.url");
        logger.info("Using Solr server {}", serverPath);
        this.serverUrl = serverPath;
        this.solrClient = new HttpSolrClient(serverUrl);        
        extensionLst = new ArrayList(Arrays.asList(conf.getProps().getProperty("data.file.ext").split(",")));
        mapper = new XmlMapper();
        
    }

    public void pushData() {
        String corpusDataPath = conf.getProps().getProperty("data.path");
        //iterate through the directory and push every file to the index server
        Path dataPath = FileSystems.getDefault().getPath(corpusDataPath);
        
        try {
            Files.walkFileTree(dataPath, new FileVisitor<Path>() {
                int totalCount = 0;

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    totalCount++;
                    String ext = getFileExtension(file.toFile());
                    boolean extensionIsValid = extensionLst.contains(ext);
                    if (extensionIsValid) {
                        SolrInputDocument doc = getDocument(file.toFile());
                        if (doc != null) {
                            pushDocument(doc);
                        } else {
                        	logger.warn("Error processing file {}", file);
                        }
                    }
                    if ((totalCount % 1000) == 0) {
                    	logger.info("Processed {} documents...");
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                	logger.warn("Could not read file '{}'", file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });

        } catch (IOException ex) {
        	logger.warn("Could not iterate through resource directory '{}'", corpusDataPath, ex);
        }

    }

    /**
     * Method to map inputFile to to Solr fields
     *
     * @param inputFile
     * @return
     */
    public SolrInputDocument getDocument(File inputFile) {
        SolrInputDocument doc = new SolrInputDocument();
        InputXmlDoc xmlDoc = null;
        try {
            xmlDoc = mapper.readValue(inputFile, InputXmlDoc.class);
            //map field to value
            doc.setField(FIELDS.id, xmlDoc.getId());
            doc.setField(FIELDS.ui, xmlDoc.getUi());
            doc.setField(FIELDS.ji, xmlDoc.getJi());
            doc.setField(FIELDS.bm, xmlDoc.getBm());
            doc.setField(FIELDS.fm, xmlDoc.getFm());
        } catch (IOException ex) {
        	logger.warn("IOException: could not deserialize file {}", inputFile, ex);
            return null;
        }
        
        return doc;
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf("."));

        } catch (Exception e) {
            return "";
        }

    }

    public void pushDocument(SolrInputDocument solrDoc) {
        try {
            solrClient.add(solrDoc, commitWithinMs);
        } catch (SolrServerException ex) {
            logger.error("SolrServerException: could not push document {}", ex);
        } catch (IOException ex) {
            logger.error("IOException: could not push document {}" + ex.getMessage());
        }
    }

}

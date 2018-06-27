package com.engine.biomine.indexing;

import com.engine.biomine.docparsing.literature.BioentityExtractor;
import com.engine.biomine.docparsing.literature.PubMedExtractor;
import com.engine.biomine.docparsing.literature.PMCExtractor;
import com.engine.biomine.docparsing.literature.DocumentExtractor;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.engine.biomine.common.*;
import com.engine.biomine.common.doc.BiomineDoc;
import com.engine.biomine.docparsing.*;
import com.engine.biomine.indexing.tasks.IndexDocsTask;

import com.google.common.collect.Lists;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexManager {

    private final Properties props;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //indexing metrics
    private final int defaultCommitWithinMs;
    private boolean fakePush;
    static final MetricRegistry metrics = new MetricRegistry();
    private final Meter docsMeter = metrics.meter("Num. indexed documents");
    private Slf4jReporter reporter;
    private final ExecutorService executor;
    private final IndexerStatus indexerStats;

    // document parsers
    private final PMCExtractor mapperPMC;
    private final PubMedExtractor mapperPubMed;
    private final AnnotationExtractor mapperAnnotation;
    private final GffExtractor mapperGff;
    private final FastaExtractor mapperFasta;
    private final BioentityExtractor mapperEntity;

    //solr interface
    private final String serverPath;
    private final CloudSolrClient solrClient;
    private final String defaultCollection;

    private final HashMap<String,String> genomeNameMap;
    //private final String morphlineConfig;

    public IndexManager() {
        props = Configs.getInstance().getProps();
        serverPath = props.getProperty("server.url");
        logger.info("Using Solr server {}", serverPath);
        this.solrClient = new CloudSolrClient.Builder().withZkHost(serverPath).build();

        String collection = props.getProperty("collections");
        defaultCollection = (collection.contains(",")) ? collection.substring(0, collection.indexOf(",")) : collection;
        solrClient.setDefaultCollection(defaultCollection);

        mapperPMC = new PMCExtractor();
        mapperPubMed = new PubMedExtractor();
        mapperGff = new GffExtractor();
        mapperFasta = new FastaExtractor();
        mapperAnnotation = new AnnotationExtractor();
        mapperEntity = new BioentityExtractor();

        fakePush = Boolean.valueOf(props.getProperty("fake.push", "false"));
        getIndexingRate();
        indexerStats = new IndexerStatus();

		//LJL:
        defaultCommitWithinMs = Integer.valueOf(props.getProperty("default.commitWhithinInMs", "500"));

        genomeNameMap = IOUtil.getINSTANCE().loadTabMapFile(props.getProperty("genomeMap.path"));
        executor = Executors.newFixedThreadPool(10);
    }

    /**
     * push doc to the index
     * @param
     */
    public void pushData(File file, boolean deleteFile, String collection) {

        File[] fileList = null;

        if(file.isDirectory())   fileList = file.listFiles();
        else fileList = new File[]{file};

        IndexDocsTask indexTask = new IndexDocsTask(this, fileList, deleteFile, collection, indexerStats);
        executor.submit(indexTask);

        //count the number of files to process
//        DocCounterTask countDocumentTask = new DocCounterTask(this, fileList, indexerStats);
//        executor.submit(countDocumentTask);
    }

    /*
     * @param inputFile
     * @return list of Solr documents to push
     */
    public ArrayList<SolrInputDocument> getSolrDocsFromSingleFile(File inputFile, String collection) {

        ArrayList<SolrInputDocument> solrDocs = new ArrayList<>();
        ArrayList<BiomineDoc> documents = new ArrayList<>();

        String ext = IOUtil.getINSTANCE().getFileExtension(inputFile);
        collection = collection.toLowerCase();

        switch(collection){
            case("literature"):
                documents = getMapper(ext).extractDocValues(inputFile);
                break;
            case("csfgentity"):
                documents = mapperEntity.extractDocValues(inputFile);
                break;
            case("csfggff"):
                documents = mapperGff.extractDocValues(inputFile, collection, genomeNameMap);
                break;
            case("csfgfasta"):
                documents = mapperFasta.extractDocValues(inputFile,collection,genomeNameMap);
                break;
            case("csfgannotation"):
                documents = mapperAnnotation.extractDocValues(inputFile,collection,genomeNameMap);
                break;
        }

        for (BiomineDoc article : documents) {
            article.setId("");
            solrDocs.add(article.toSolrDoc());
        }

        return solrDocs;
    }

    /**
     *
     * @param ext
     * @return
     */
    private DocumentExtractor getMapper(String ext){
        switch(ext){
            case(".gz"):
            case(".xml"):
                return mapperPubMed;

            case(".nxml"):
            case(".tar.gz"):
            case(".tgz"):
                return mapperPMC;

            default:
                return mapperPubMed;
        }
    }

    public boolean sendDocToIndex (SolrInputDocument doc, String collection, String fileName) {

        boolean result = true;

        if (doc != null && doc.size() > 0) {
            try {
                docsMeter.mark();
//                pushDocument(doc, collection);
                pushDocumentAndCommit(doc, collection);
            } catch (Exception e) {
                result = false;
                logger.error("Could not push doc for file {}", fileName, e);
            }
        } else {
            indexerStats.incrementWarnings();
            result = false;
        }
        return result;
    }


    public void pushDocument(SolrInputDocument solrDoc, String collection) {

        if (fakePush == false) {
            try {
                this.solrClient.add(collection, solrDoc);
            } catch (SolrServerException ex) {
                logger.error("SolrServerException: could not push doc {}", ex);
            } catch (IOException ex) {
                logger.error("IOException: could not push doc {}" + ex.getMessage());
            }
        }
    }

	public void pushDocumentAndCommit(SolrInputDocument solrDoc, String collection) {

        if (fakePush == false) {
            try {
                this.solrClient.add(collection, solrDoc, defaultCommitWithinMs);
            } catch (SolrServerException ex) {
                logger.error("SolrServerException: could not push doc {}", ex);
            } catch (IOException ex) {
                logger.error("IOException: could not push doc {}" + ex.getMessage());
            }
        }
    }


    private void getIndexingRate() {
        //reporter indexing rate
        reporter = Slf4jReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        //outputs the indexing rate every minute
        reporter.start(1, TimeUnit.MINUTES);
    }

    public IndexerStatus getStatus() {
        return indexerStats;
    }

    public boolean deleteDocs(String collection,String... docIds) {
        boolean res = false;
        //remove documents
        ArrayList docIdLst = Lists.newArrayList(docIds);
        try {
            this.solrClient.deleteById(collection, docIdLst, defaultCommitWithinMs);
            this.solrClient.commit();
            res = true;
            logger.info("Documents removed from index: {}", docIdLst.size());
        } catch (SolrServerException | IOException ex) {
            logger.error("Failed to delete documents from the index", ex);
        }
        return res;
    }


    //------------------------------------------------
    //----------------Only used for DocCounter
    //------------------------------------------------
    public long getDocumentsCount(File inputFile) {
        long count = 0;
        if (IOUtil.getINSTANCE().getFileExtension(inputFile).contains(".gz")) {
            //the xml doc contains multiple child docs
            count = mapperPubMed.getArticleCount(inputFile);
        } else {
            count =1;
        }
        return count;
    }
    //------------------------------------------------



}

package com.engine.biomine.indexing.tasks;

import com.engine.biomine.indexing.IndexManager;
import com.engine.biomine.indexing.IndexerStatus;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by halmeida on 10/20/16.
 */
public class DocThread implements Runnable {

    IndexManager manager;
    IndexerStatus monitor;
    File file;
    String collection;

    public DocThread(IndexManager manager, IndexerStatus monitor, File file, String collection){
        this.manager = manager;
        this.monitor = monitor;
        this.file = file;
        this.collection = collection;
    }

    public void run(){
        processFile();
    }

    private void processFile(){

        ArrayList<SolrInputDocument> docArray = manager.getSolrDocsFromSingleFile(file, collection);
        monitor.incrementNbDocsToProcess(docArray.size());

        for (SolrInputDocument singleDoc : docArray) {
            manager.sendDocToIndex(singleDoc, collection, file.getName());
            this.monitor.incrementNbProcessedDocs();
        }

        if(file.getAbsolutePath().contains("tmp")) file.delete();
    }
}

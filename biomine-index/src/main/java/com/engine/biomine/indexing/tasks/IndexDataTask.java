package com.engine.biomine.indexing.tasks;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import com.engine.biomine.common.IOUtil;
import com.engine.biomine.indexing.IndexManager;
import com.engine.biomine.indexing.IndexerStatus;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import de.schlichtherle.truezip.file.TFile;
import org.apache.solr.common.SolrInputDocument;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.kitesdk.morphline.shaded.com.google.common.reflect.ClassPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.kitesdk.morphline.api.Command;
import org.kitesdk.morphline.api.MorphlineContext;
import org.kitesdk.morphline.api.Record;
import org.kitesdk.morphline.base.Fields;
import org.kitesdk.morphline.base.Notifications;
import org.kitesdk.morphline.base.Compiler;

/**
 * Created by halmeida on 7/8/16.
 */
@Deprecated
public class IndexDataTask implements Callable<Boolean>{

    private final IndexManager indexManager;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final IndexerStatus monitor;
    private final File morphlineConfig;
    private final String collection;
    private final File[] dataFileList;
    private final boolean deleteFile;

    public IndexDataTask(IndexManager indexManager, String config, File[] fileList, boolean deleteFile, String collection, IndexerStatus stats) {
        this.indexManager = indexManager;
        this.morphlineConfig = new File(config);
        this.dataFileList = fileList;
        this.collection = collection;
        this.monitor = stats;
        this.deleteFile = deleteFile;
    }

    @Override
    public Boolean call() {
        boolean response = false;
        String morphlineId = "biomine";
        //object to collect pipe processed records
        Collector collector = new Collector();

        MorphlineContext morphlineContext = new MorphlineContext.Builder().build();
        Command morphline = null;

//        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
//        ClassPath classPath = null;
//        try {
//            classPath = ClassPath.from(classLoader);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        ImmutableSet<ClassPath.ClassInfo> set = classPath.getTopLevelClasses();
//        for (ClassPath.ClassInfo ci : set) {
//            logger.info("lalala: {}",ci.getName());
//        }

        try {
            morphline = new Compiler().compile(morphlineConfig, morphlineId, morphlineContext, collector);
        }catch(Exception e) {
            logger.warn("Morphlines: ", e);
        }
        //detect file type
        //for not compressed files, mimetype was generic [text/plain]
        TikaConfig tikaconf = TikaConfig.getDefaultConfig();
        Detector detec = tikaconf.getDetector();

        for(File dataFile : dataFileList) {
            logger.debug("Processing file {}", dataFile.getName());
            if (IOUtil.getINSTANCE().isValidExtension(dataFile.getName())) {
                try {

//                    TFile[] thisfile = IOUtil.getINSTANCE().getFileStream(dataFile);
//                    File onefile = thisfile[0].listFiles()[0];
                    File onefile = new File(dataFile.getName());
                    List<String> testList = IOUtil.getINSTANCE().loadFileWithMultiLine(onefile, '>');
                    //check what is the mimetype for the current file
                    TikaInputStream tki = TikaInputStream.get(dataFile);
                    Metadata meta = new Metadata();
                    meta.add(Metadata.RESOURCE_NAME_KEY, dataFile.getName());

                    //set proper mimetype so it is processed accordingly
                    MediaType mtype = detec.detect(tki, meta);
                    String mimeType = adjustMimeType(mtype.toString(), dataFile);

                    // process each input data file
                    InputStream in = new BufferedInputStream(new FileInputStream(dataFile));
                    Record record = new Record();
                    record.put(Fields.ATTACHMENT_BODY, in);

                    //inform final mimetype to morphline pipe
                    record.put(Fields.ATTACHMENT_MIME_TYPE, mimeType);
                    logger.warn("Starting to process record.");
                    //get records from file
                    response = morphline.process(record);

                    if (!response) logger.warn("Failed to process record with morphlines: " + record);
                    else logger.warn("Finished to process record with morphlines: {}. ", dataFile.getName());
                    tki.close();
                    in.close();

                } catch (RuntimeException | IOException e) {
                    morphlineContext.getExceptionHandler().handleException(e, null);
                    logger.warn("Could not process file {}", dataFile);
                }

                Iterator<Record> iter = collector.getRecords().iterator();

                int size = collector.getRecords().size();
                logger.info("Compressed fileList contains: {} records ", size);
                while (iter.hasNext()) {
                    Record thisRecord = iter.next();
//                    SolrInputDocument doc = indexManager.getDocument(thisRecord, collection, this.monitor.getNbProcessed());
//                    response = indexManager.sendDocToIndex(doc, collection, dataFile.getName());
                    this.monitor.incrementNbProcessedDocs();
                    if (response && (this.monitor.getNbProcessed() % 1000==0 )) {
                        logger.warn("Finished record {} out of {}.", this.monitor.getNbProcessed(), size);
                    }
                }

                logger.warn("Finished fileList {}: {} documents indexed out of {}.", dataFile, monitor.getNbProcessed(), size);
                if (deleteFile) dataFile.delete();
            }
            else logger.error("File extension not accepted as valid doc: {}", dataFile);

        }
        return true;
    }

    /**
     * Adjusts morphline mimetype according
     * to file extension.
     * Mimetype is used to tell which
     * morphline command will be executed
     *
     * @param mimeType
     * @param file
     * @return
     */
    private String adjustMimeType(String mimeType, File file){
        if(mimeType.contains("text/plain")){
            String ext = IOUtil.getINSTANCE().getFileExtension(file);

            if(ext.contains(".gff")){
                mimeType = "text/gff";
            }
            //Protein sequence
            else if(ext.contains(".faa")){
                mimeType = "text/faa";
            }
            // RNA sequence
            else if(ext.contains(".fna") ||
                    ext.contains(".fa") ||
                    ext.contains(".fasta")){
                mimeType = "text/fna";
            }
        }
        return mimeType;
    }


    /**************************************
     * From morphlines API tests:
     * Collector.java
     * morphlines/kite/kite-morphlines/kite-morphlines-core/src/test/java/org/kitesdk/morphline/api
     **************************************/
    class Collector implements Command {

        private Command parent;
        private List<Record> records;
        private int numStartEvents;

        public Collector() {
            reset();
        }
        public void reset() {
            records = new ArrayList<Record>();
            numStartEvents = 0;
        }
        @Override
        public Command getParent() {
            return parent;
        }
        @Override
        public void notify(Record notification) {
            if (Notifications.containsLifecycleEvent(notification, Notifications.LifecycleEvent.START_SESSION)) {
                numStartEvents++;
            }
        }
        @Override
        public boolean process(Record record) {
            Preconditions.checkNotNull(record);
            records.add(record);
            return true;
        }
        public List<Record> getRecords() {
            return records;
        }
        public Record getFirstRecord() {
            if (records.size() != 1) {
                throw new IllegalStateException();
            }
            if (records.get(0) == null) {
                throw new IllegalStateException();
            }
            return records.get(0);
        }
        public int getNumStartEvents() {
            return numStartEvents;
        }
    }

}

package com.engine.biomine.indexing.tasks;

import com.engine.biomine.common.IOUtil;
import com.engine.biomine.indexing.IndexManager;
import com.engine.biomine.indexing.IndexerStatus;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by halmeida on 9/1/16.
 */
@Deprecated
public class IndexBiodataTask implements Callable<Boolean> {


    private final IndexManager indexManager;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final IndexerStatus monitor;
    private final String collection;
    private final File[] dataFileList;
    private final boolean deleteFile;
    ExecutorService exec;
    private int nbThreads = 20;

    public IndexBiodataTask(IndexManager indexManager, File[] fileList, boolean deleteFile, String collection, IndexerStatus stats) {
        this.indexManager = indexManager;
        this.dataFileList = fileList;
        this.collection = collection;
        this.monitor = stats;
        this.deleteFile = deleteFile;
        exec = Executors.newFixedThreadPool(nbThreads);
    }
    @Override
    public Boolean call() {

        for (File dataFile : dataFileList) {
            logger.debug("Processing file {}", dataFile.getName());
            if (IOUtil.getINSTANCE().isValidExtension(dataFile.getName())) {

                switch (IOUtil.getINSTANCE().getFileExtension(dataFile)) {
                    case (".tgz"):
                    case (".tar.gz"):
                        TarArchiveInputStream tis = (TarArchiveInputStream) IOUtil.getINSTANCE().getFileStream(dataFile);
                        this.processArchiveFile(dataFile, tis);
                        break;
                    case (".zip"):
                        ZipArchiveInputStream zis = (ZipArchiveInputStream) IOUtil.getINSTANCE().getFileStream(dataFile);
                        this.processArchiveFile(dataFile, zis);
                    case (".fasta"):
                    case (".fna"):
                    case (".faa"):
                    case (".fa"):
                    case (".gff3"):
                        this.processFile(dataFile);
                      break;

                    default:
                        logger.error("File extension not accepted as valid doc: {}", dataFile);

                }
                if (deleteFile) dataFile.delete();
            }
        }
        return true;
    }

    private void processFile(File file){
//        Runnable docThread = new DocThread(indexManager, monitor, file, collection);
//        exec.execute(docThread);
    }

    //process tar.gz, tgz, zip
    private void processArchiveFile(File archiveFile, ArchiveInputStream ais){

        try {
            for (ArchiveEntry entry = ais.getNextEntry(); entry != null; ) {
                if (!entry.isDirectory()) {
                    File currentfile = IOUtil.getINSTANCE().retrieveCompressedFile(ais, entry.getName());

                    if (currentfile != null) {
//                        Runnable docThread = new DocThread(indexManager, monitor, currentfile, collection);
//                        exec.execute(docThread);
//                        monitor.incrementNbDocsToProcess();
                    }
                }
                entry = ais.getNextEntry();
            }
            ais.close();

        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.warn("Done reading file {}: {} documents to process.", archiveFile, monitor.getNbDocsToProcess());

    }

}

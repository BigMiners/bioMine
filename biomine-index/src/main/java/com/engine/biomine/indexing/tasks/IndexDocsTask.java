/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.engine.biomine.indexing.tasks;

import com.engine.biomine.common.IOUtil;
import com.engine.biomine.indexing.IndexManager;
import com.engine.biomine.indexing.IndexerStatus;

import java.io.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ludovic
 */
public class IndexDocsTask implements Callable<Boolean> {

    private final IndexManager indexManager;
    private final File[] fileList;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final IndexerStatus monitor;
    private final String collection;
    private final boolean deleteFile;
    private int nbThreads = 20;
    ExecutorService exec;

    public IndexDocsTask(IndexManager indexManager, File[] fileList, boolean deleteFile, String collection, IndexerStatus stats) {
        this.fileList = fileList;
        this.collection = collection;
        this.indexManager = indexManager;
        this.monitor = stats;
        this.deleteFile = deleteFile;
        exec = Executors.newFixedThreadPool(nbThreads);
    }

    @Override
    public Boolean call() {

        for(File file : fileList) {
            if (IOUtil.getINSTANCE().isValidExtension(file.getName())) {
                switch (IOUtil.getINSTANCE().getFileExtension(file)) {
                    case (".gz"):
                    case (".xml"):
                    case (".nxml"):
//                        exec = Executors.newFixedThreadPool(nbThreads);
                        this.processFile(file);
                        break;
                    case (".fasta"):
                    case (".fna"):
                    case (".faa"):
                    case (".fa"):
                    case (".gff3"):
//                        exec = Executors.newFixedThreadPool(5);
                        this.processFile(file);
                        break;

                    case (".zip"):
//                        exec = Executors.newFixedThreadPool(5);
                        ZipInputStream zis = (ZipInputStream) IOUtil.getINSTANCE().getFileStream(file);
                        this.processZipFile(file, zis);
                        break;

                    case (".tar"):
                    case (".tgz"):
                    case (".tar.gz"):
//                        exec = Executors.newFixedThreadPool(nbThreads);
                        TarArchiveInputStream tis = (TarArchiveInputStream) IOUtil.getINSTANCE().getFileStream(file);
                        this.processArchiveFile(file, tis);
                        break;

                    default:
                        logger.error("File extension not accepted as valid doc: {}", file);
                }

                if (deleteFile) file.delete();
            }
        }
        return true;
    }

    private void processFile(File file){
        if(file != null) {
            Runnable docThread = new DocThread(indexManager, monitor, file, collection);
            exec.execute(docThread);
        }
    }

    private void processZipFile(File archiveFile, ZipInputStream zis){
       try {
           for (ZipEntry entry = zis.getNextEntry(); entry != null; ){
               if (!entry.isDirectory()) {
                   File currentfile = IOUtil.getINSTANCE().retrieveCompressedFile(zis, entry.getName());
                   if (currentfile != null) {
                       Runnable docThread = new DocThread(indexManager, monitor, currentfile, collection);
                       exec.execute(docThread);
                   }
               }
               entry = zis.getNextEntry();
           }
           zis.close();
       }catch (IOException e) {
           e.printStackTrace();
       }
        logger.warn("Done reading file {}: . Sent to process.", archiveFile, monitor.getNbDocsToProcess());
    }


    private void processArchiveFile(File archiveFile, ArchiveInputStream ais){
        try {

                for (ArchiveEntry entry = ais.getNextEntry(); entry != null; ) {
                    if (!entry.isDirectory()) {
                        File currentfile = IOUtil.getINSTANCE().retrieveCompressedFile(ais, entry.getName());
                        if (currentfile != null) {
                            Runnable docThread = new DocThread(indexManager, monitor, currentfile, collection);
                            exec.execute(docThread);
//                            monitor.incrementNbDocsToProcess();
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






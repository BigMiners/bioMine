/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.engine.biomine.indexing.tasks;

import com.engine.biomine.common.IOUtil;
import com.engine.biomine.indexing.IndexManager;
import com.engine.biomine.indexing.IndexerStatus;
import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.fs.archive.tar.TarBZip2Driver;
import de.schlichtherle.truezip.fs.archive.tar.TarDriver;
import de.schlichtherle.truezip.fs.archive.tar.TarGZipDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ludovic
 */
public class DocCounterTask implements Callable<Boolean> {

    private final IndexManager indexManager;
    private final File[] fileList;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final IndexerStatus monitor;

    public DocCounterTask(IndexManager indexManager, File[] fileList, IndexerStatus stats) {
        this.fileList = fileList;
        this.indexManager = indexManager;
        this.monitor = stats;
    }

    @Override
    public Boolean call() {

        for(File file : fileList) {
            if (IOUtil.getINSTANCE().isValidExtension(file.getName())) {
                String ext = IOUtil.getINSTANCE().getFileExtension(file);

                //boolean flag that indicates if the file is of type gz. tgz or tar.gz
                boolean archiveForTrueZip = false;
                boolean archiveForGZip = false;

                if (ext.equals(".tgz") || ext.equals(".tar.gz")) {
                    archiveForTrueZip = true;
                }
                if (ext.equals(".gz") || ext.equals(".xml")) {
                    archiveForGZip = true;
                }

                //process single XML file
                //handle compressed files with single XML with multiple articles (PubMed)
                if (archiveForGZip) {
                    long count = indexManager.getDocumentsCount(file);
                    this.monitor.incrementNbDocsToProcess(count);
                } //handle archive files with multiple NXMLs with single articles (PMC)
                else if (archiveForTrueZip) {
//                    TFile[] directories = IOUtil.getINSTANCE().decompressTarGz(file);
//                    for (File currentDir : directories) {
//                        for (File currentfile : currentDir.listFiles()) {
//                            if (IOUtil.getINSTANCE().isValidExtension(currentfile.getName())) {
//                                this.monitor.incrementNbDocsToProcess();
//                            }
//                        }
//                    }
                } else {
                    long count = indexManager.getDocumentsCount(file);
                    this.monitor.incrementNbDocsToProcess(count);
                }
            }
        }
        return true;
    }

}

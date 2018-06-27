/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.engine.biomine.docparsing.literature;

import com.engine.biomine.common.doc.BiomineDoc;
import java.io.InputStream;
import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ludovic
 */
public class DocumentExtractorTest {

    private final Logger logger = LoggerFactory.getLogger(DocumentExtractorTest.class);

    public DocumentExtractorTest() {
    }

    /**
     * Test of extractDocValues method, of class DocumentExtractor.
     */
    @Test
    public void testExtractDocValues_InputStream() {
        logger.info("Testing PMC extractor: extractDocValues");
        InputStream inputstream = DocumentExtractorTest.class.getResourceAsStream("/multipleFileSample.xml");

        DocumentExtractor instance = new PMCExtractor();
        int expResult = 1906;
        ArrayList<BiomineDoc> result = instance.extractValuesFromDocs(inputstream);
        assertEquals(expResult, result.size());

        InputStream inputstreamSingleDoc = DocumentExtractorTest.class.getResourceAsStream("/ACS_Chem_Biol_2014_Aug_15_9(8)_1826-1833.nxml");
        ArrayList<BiomineDoc> resultSingleDoc = instance.extractValuesFromDocs(inputstreamSingleDoc);
        assertEquals(1, resultSingleDoc.size());

    }
}

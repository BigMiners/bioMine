package com.engine.biomine.docparsing;

import com.engine.biomine.common.FIELDS;
import com.engine.biomine.common.IOUtil;
import com.engine.biomine.common.doc.BiomineDoc;
import com.engine.biomine.common.doc.FastaDoc;
import org.apache.commons.lang.StringUtils;
import org.kitesdk.morphline.api.Record;

import java.io.File;
import java.util.*;

/**
 * Created by halmeida on 8/15/16.
 */
public class FastaExtractor {

    private final String separator = "\t";

    /**
     * extracts biomine fields from a record
     * @param
     * @return
     */
    public ArrayList<BiomineDoc> extractDocValues(File record, String collection, HashMap<String,String> genomeMap) {
        ArrayList<BiomineDoc> result = new ArrayList<>();

       List<String> toProcess = IOUtil.getINSTANCE().loadFileWithMultiLine(record, '>');
        String ext = IOUtil.getINSTANCE().getFileExtension(record);

        for(int i = 0; i < toProcess.size(); i++){
            FastaDoc thisDoc = extractRecordValues(toProcess.get(i), ext);
            thisDoc.setId(String.valueOf(i));

            if(collection.toLowerCase().contains("csfg"))
                thisDoc.setProvenance("CSFG");

            if(genomeMap.containsKey(thisDoc.getGenomeVersion()))
                thisDoc.setGenomeName(genomeMap.get(thisDoc.getGenomeVersion()));

            result.add(thisDoc);
        }

        return result;
    }


    private FastaDoc extractRecordValues(String oneRecord, String ext){
        FastaDoc doc = new FastaDoc();
        String recordValues[] = StringUtils.split(oneRecord,"\t");

        String attValue = recordValues[0];
        attValue = attValue.replaceAll(">", "");
        doc.setAttributeId(attValue);

        if(attValue.contains("_")){
            String acronym = attValue.substring(0, attValue.indexOf("_"));
            if(acronym.length() > 6) acronym = acronym.substring(0, 6);
            doc.setGenomeVersion(acronym);
        }

        switch(ext){
            case(".faa"):
                doc.setProteinSeq(recordValues[1]);
                break;
            case(".fa"):
                doc.setScaffoldSeq(recordValues[1]);
                break;
            default:
                doc.setGeneModelSeq(recordValues[1]);
        }

        return doc;
    }

    //morphlines input method
    public FastaDoc extractRecords(Record record) {
        FastaDoc doc = new FastaDoc();

        if (record != null) {
            Map<String, Collection<Object>> recordContent = record.getFields().asMap();

            if(recordContent.keySet().contains(FIELDS.attributeId)){
                Collection<Object> value = recordContent.get(FIELDS.attributeId);
                if(value.iterator().hasNext()) {
                    String attribute = (String) value.iterator().next();
                    attribute = attribute.replaceAll(">","");
                    doc.setField(FIELDS.attributeId, attribute);

                    if(attribute.contains("_")){
                      String acronym = attribute.substring(0, attribute.indexOf("_"));
                      if(acronym.length() > 6) acronym = acronym.substring(0, 6);
                      doc.setField(FIELDS.genomeVersion, acronym);
                    }
                }
            }
            if(recordContent.keySet().contains(FIELDS.proteinSeq)){
                Collection<Object> value = recordContent.get(FIELDS.proteinSeq);
                if(value.iterator().hasNext()) {

                    String seq = (String) value.iterator().next();
                    seq = seq.replace(System.getProperty("line.separator"), "");

                    doc.setField(FIELDS.proteinSeq, seq);
                }
            }
            if(recordContent.keySet().contains(FIELDS.geneModelSeq)){
                Collection<Object> value = recordContent.get(FIELDS.geneModelSeq);
                if(value.iterator().hasNext()) {

                    String seq = (String) value.iterator().next();
                    seq = seq.replace(System.getProperty("line.separator"), "");

                    doc.setField(FIELDS.geneModelSeq, seq);
                }
            }
        }
        return doc;
    }
}

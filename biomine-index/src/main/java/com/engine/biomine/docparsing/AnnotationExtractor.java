package com.engine.biomine.docparsing;

import com.engine.biomine.common.FIELDS;
import com.engine.biomine.common.doc.AnnotationDoc;
import com.engine.biomine.common.doc.BiomineDoc;
import org.kitesdk.morphline.api.Record;

import java.io.File;
import java.util.*;

/**
 * Created by halmeida on 8/17/16.
 */
public class AnnotationExtractor {


    public ArrayList<BiomineDoc> extractDocValues(File record, String collection, HashMap<String,String> genomeMap) {
        ArrayList<BiomineDoc> result = new ArrayList<>();
        return result;
    }


    public AnnotationDoc extractRecordValues(String oneRecord){
        AnnotationDoc doc = new AnnotationDoc();

        return doc;
    }


    //morphlines input method
    public AnnotationDoc extractRecords(Record document) {

        AnnotationDoc doc = new AnnotationDoc();

        if (document != null) {
            Map<String, Collection<Object>> recordContent = document.getFields().asMap();

            if(recordContent.keySet().contains(FIELDS.proteinId)){
                Collection<Object> value = recordContent.get(FIELDS.proteinId);
                if(value.iterator().hasNext()) {
                    String indexValue = (String) value.iterator().next();
                    doc.setField(FIELDS.proteinId,indexValue);
                }
            }
            if(recordContent.keySet().contains(FIELDS.proteinLength)){
                Collection<Object> value = recordContent.get(FIELDS.proteinLength);
                if(value.iterator().hasNext()) {
                    int indexValue;
                    try {
                        indexValue = Integer.parseInt((String) value.iterator().next());
                    }catch (NumberFormatException e){
                        indexValue = 0;
                    }
                    doc.setField(FIELDS.proteinLength,indexValue);
                }
            }
            if(recordContent.keySet().contains(FIELDS.geneName)){
                Collection<Object> value = recordContent.get(FIELDS.geneName);
                if(value.iterator().hasNext()) {
                    String indexValue = (String) value.iterator().next();
                    doc.setField(FIELDS.geneName,indexValue);
                }
            }
            if(recordContent.keySet().contains(FIELDS.description)){
                Collection<Object> value = recordContent.get(FIELDS.description);
                if(value.iterator().hasNext()) {
                    String indexValue = (String) value.iterator().next();
                    doc.setField(FIELDS.description,indexValue);
                }
            }
            if(recordContent.keySet().contains(FIELDS.evidence)){
                Collection<Object> value = recordContent.get(FIELDS.evidence);
                if(value.iterator().hasNext()) {
                    String indexValue = (String) value.iterator().next();
                    doc.setField(FIELDS.evidence,indexValue);
                }
            }
            if(recordContent.keySet().contains(FIELDS.evidenceCode)){
                Collection<Object> value = recordContent.get(FIELDS.evidenceCode);
                if(value.iterator().hasNext()) {
                    String indexValue = (String) value.iterator().next();
                    doc.setField(FIELDS.evidenceCode,indexValue);
                }
            }
            if(recordContent.keySet().contains(FIELDS.cazy)){
                Collection<Object> value = recordContent.get(FIELDS.cazy);
                if(value.iterator().hasNext()) {
                    String indexValue = (String) value.iterator().next();
                    doc.setField(FIELDS.cazy,indexValue);
                }
            }
            if(recordContent.keySet().contains(FIELDS.cbm)){
                Collection<Object> value = recordContent.get(FIELDS.cbm);
                if(value.iterator().hasNext()) {
                    String indexValue = (String) value.iterator().next();
                    doc.setField(FIELDS.cbm,indexValue);
                }
            }
            if(recordContent.keySet().contains(FIELDS.gpi)){
                Collection<Object> value = recordContent.get(FIELDS.gpi);
                if(value.iterator().hasNext()) {
                    boolean indexValue = Boolean.valueOf((String)value.iterator().next());
                    doc.setField(FIELDS.gpi,indexValue);
                }
            }
            if(recordContent.keySet().contains(FIELDS.kdel)){
                Collection<Object> value = recordContent.get(FIELDS.kdel);
                if(value.iterator().hasNext()) {
                    boolean indexValue = Boolean.valueOf((String)value.iterator().next());
                    doc.setField(FIELDS.kdel,indexValue);
                }
            }
            if(recordContent.keySet().contains(FIELDS.wolfPsort)){
                Collection<Object> value = recordContent.get(FIELDS.wolfPsort);
                if(value.iterator().hasNext()) {
                    String indexValue = (String) value.iterator().next();
                    doc.setField(FIELDS.wolfPsort,indexValue);
                }
            }
            if(recordContent.keySet().contains(FIELDS.spLength)){
                Collection<Object> value = recordContent.get(FIELDS.spLength);
                if(value.iterator().hasNext()) {
                    int indexValue;
                    try {
                        indexValue = Integer.parseInt((String) value.iterator().next());
                    }catch (NumberFormatException e){
                        indexValue = 0;
                    }
                    doc.setField(FIELDS.spLength,indexValue);
                }
            }
            if(recordContent.keySet().contains(FIELDS.goFunction)){
                Collection<Object> value = recordContent.get(FIELDS.goFunction);
                if(value.iterator().hasNext()) {
                    String indexValue = (String) value.iterator().next();
                    doc.setField(FIELDS.goFunction,indexValue);
                }
            }
            if(recordContent.keySet().contains(FIELDS.goComponent)){
                Collection<Object> value = recordContent.get(FIELDS.goComponent);
                if(value.iterator().hasNext()) {
                    String indexValue = (String) value.iterator().next();
                    doc.setField(FIELDS.goComponent,indexValue);
                }
            }
            if(recordContent.keySet().contains(FIELDS.goProcess)){
                Collection<Object> value = recordContent.get(FIELDS.goProcess);
                if(value.iterator().hasNext()) {
                    String indexValue = (String) value.iterator().next();
                    doc.setField(FIELDS.goProcess,indexValue);
                }
            }
        }



        return doc;
    }
}

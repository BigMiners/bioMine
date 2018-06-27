package com.engine.biomine.docparsing;

import com.engine.biomine.common.FIELDS;
import com.engine.biomine.common.IOUtil;
import com.engine.biomine.common.doc.BiomineDoc;
import com.engine.biomine.common.doc.GffDoc;
import org.apache.commons.lang.StringUtils;
import org.kitesdk.morphline.api.Record;

import java.io.File;
import java.util.*;

/**
 * Created by halmeida on 8/15/16.
 */
public class GffExtractor {

    private final String separator = "\t";


    public ArrayList<BiomineDoc> extractDocValues(File record, String collection, HashMap<String,String> genomeMap) {
        ArrayList<BiomineDoc> result = new ArrayList<>();
        List<String> toProcess = IOUtil.getINSTANCE().loadFileWithSeparator(record, false, '0');

        //split each item of list, and record according to position in split.
        for(int i = 0; i < toProcess.size(); i++){
            GffDoc thisDoc = extractRecordValues(toProcess.get(i));

            thisDoc.setId(String.valueOf(i));

            if(collection.toLowerCase().contains("csfg"))
                thisDoc.setProvenance("CSFG");

            if(genomeMap.containsKey(thisDoc.getGenomeVersion()))
            thisDoc.setGenomeName(genomeMap.get(thisDoc.getGenomeVersion()));

            result.add(thisDoc);
        }
        return result;
    }

    public GffDoc extractRecordValues(String oneRecord){
        GffDoc doc = new GffDoc();
        String recordValues[] = StringUtils.split(oneRecord,"\t");

        for(int i = 0; i < GffDoc.Fields.values().length; i++){
            String field = GffDoc.Fields.values()[i].name();

            if(recordValues[i]!= null && !recordValues[i].isEmpty()){

                switch (field){
                    case("featureStart"):
                    case("featureEnd"):
                        int intValue;
                        try {
                            intValue = Integer.parseInt(recordValues[i]);
                            doc.setField(field,intValue);
                        }catch (NumberFormatException e){
                            //indexValue = 0;
                        }
                        break;

                    case("score"):
                        float floatValue;
                        try {
                            floatValue = Float.parseFloat(recordValues[i]);
                            doc.setField(field,floatValue);
                        } catch (NumberFormatException e){
                            // indexValue = new Float(0.0);
                        }
                        break;

                    case("attributeId"):
                        String attvalue = recordValues[i].replaceAll(">","");
                        doc.setField(field,attvalue);
                        if(attvalue.contains("ID=") && attvalue.contains("_")){
                            String acronym = attvalue.substring(attvalue.indexOf("ID="),attvalue.length());
                            acronym = acronym.substring(0, acronym.indexOf("_"));
                            acronym = acronym.replace("ID=", "");
                            if(acronym.length() > 6) acronym = acronym.substring(0, 6);
                            doc.setField(FIELDS.genomeVersion, acronym);
                        }

                        break;

                    default:
                        doc.setField(field, recordValues[i]);
                        break;
                }
            }
        }

        return doc;
    }


    //morphlines input method
    public GffDoc extractDocValues(Record record) {
        GffDoc doc = new GffDoc();

        if(record != null){
            Map<String, Collection<Object>> recordContent = record.getFields().asMap();

            if(recordContent.keySet().contains(FIELDS.correspondingSeq)){
                Collection<Object> value = recordContent.get(FIELDS.correspondingSeq);
                if(value.iterator().hasNext()) {
                    String indexValue = (String) value.iterator().next();
                    doc.setField(FIELDS.correspondingSeq,indexValue);
                }
            }
            if(recordContent.keySet().contains(FIELDS.featureSource)){
                Collection<Object> value = recordContent.get(FIELDS.featureSource);
                if(value.iterator().hasNext()) {
                    String indexValue = (String) value.iterator().next();
                    doc.setField(FIELDS.featureSource,indexValue);
                }
            }
            if(recordContent.keySet().contains(FIELDS.featureName)){
                Collection<Object> value = recordContent.get(FIELDS.featureName);
                if(value.iterator().hasNext()) {
                    String indexValue = (String) value.iterator().next();
                    doc.setField(FIELDS.featureName,indexValue);
                }
            }
            if(recordContent.keySet().contains(FIELDS.featureStart)){
                Collection<Object> value = recordContent.get(FIELDS.featureStart);
                if(value.iterator().hasNext()) {
                    int indexValue;
                    try {
                        indexValue = Integer.parseInt((String) value.iterator().next());
                        doc.setField(FIELDS.featureStart,indexValue);
                    }catch (NumberFormatException e){
                        //indexValue = 0;
                    }
                }
            }
            if(recordContent.keySet().contains(FIELDS.featureEnd)){
                Collection<Object> value = recordContent.get(FIELDS.featureEnd);
                if(value.iterator().hasNext()) {
                    int indexValue;
                    try {
                        indexValue = Integer.parseInt((String) value.iterator().next());
                        doc.setField(FIELDS.featureEnd,indexValue);
                    }catch (NumberFormatException e){
                        //indexValue = 0;
                    }

                }
            }
            if(recordContent.keySet().contains(FIELDS.score)){
                Collection<Object> value = recordContent.get(FIELDS.score);
                if(value.iterator().hasNext()) {
                    float indexValue;
                    try {
                        indexValue = Float.parseFloat((String) value.iterator().next());
                        doc.setField(FIELDS.score,indexValue);
                    } catch (NumberFormatException e){
                       // indexValue = new Float(0.0);
                    }
                }
            }
            if(recordContent.keySet().contains(FIELDS.strand)){
                Collection<Object> value = recordContent.get(FIELDS.strand);
                if(value.iterator().hasNext()) {
                    String indexValue = (String) value.iterator().next();
                    doc.setField(FIELDS.strand,indexValue);
                }
            }
            if(recordContent.keySet().contains(FIELDS.phase)){
                Collection<Object> value = recordContent.get(FIELDS.phase);
                if(value.iterator().hasNext()) {
                    String indexValue = (String) value.iterator().next();
                    doc.setField(FIELDS.phase,indexValue);
                }
            }
            if(recordContent.keySet().contains(FIELDS.attributeId)){
                Collection<Object> value = recordContent.get(FIELDS.attributeId);
                if(value.iterator().hasNext()) {
                    String indexValue = (String) value.iterator().next();
                    indexValue = indexValue.replaceAll(">","");

                    doc.setField(FIELDS.attributeId,indexValue);

                    if(indexValue.contains("ID=") && indexValue.contains("_")){
                        String acronym = indexValue.substring(indexValue.indexOf("ID="),indexValue.length());
                        acronym = acronym.substring(0, acronym.indexOf("_"));
                        acronym = acronym.replace("ID=", "");
                        if(acronym.length() > 6) acronym = acronym.substring(0, 6);
                        doc.setField(FIELDS.genomeVersion, acronym);
                    }
                }
            }
        }

        return doc;
    }

}

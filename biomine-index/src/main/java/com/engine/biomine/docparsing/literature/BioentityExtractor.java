package com.engine.biomine.docparsing.literature;

import annotator.EntityAnnotator;
import common.Annotation;
import com.engine.biomine.common.Configs;
import extractor.EntityExtractor;
import com.engine.biomine.common.doc.EntityDoc;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.*;

/**
 * Created by halmeida on 9/16/16.
 */
public class BioentityExtractor extends DocumentExtractor{

    EntityAnnotator annotator;
    List<String> entities;
    List<EntityExtractor> extractors;
    String dictionaries;
    Properties props;

    public BioentityExtractor(){
        super();
        props = Configs.getInstance().getProps();
        annotator = new EntityAnnotator();
        entities = Arrays.asList(props.getProperty("entities").split(","));
        dictionaries = props.getProperty("dictionaries.path");
        extractors = EntityExtractor.getExtractors(entities,dictionaries);
    }


    @Override
    public EntityDoc extractDocValues(Document document){
        EntityDoc doc = new EntityDoc();
        String source = "";
        Node type = null;

        try{
            type = (Node) xpathExecutor.evaluate("//body", document, XPathConstants.NODE);
            if(type == null) source = "pubmed";
            else source = "pmc";
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        List<Annotation> result = annotator.process(document, source, extractors);

        if(!result.isEmpty()){
            Iterator<Annotation> iter = result.iterator();

            while(iter.hasNext()){
                Annotation thisAnnot = iter.next();
                String annotSf = thisAnnot.getSurfaceForm();
                String annotType = thisAnnot.getType();

                if(!annotSf.isEmpty())
                doc.setField(annotType,annotSf);
            }
        }

        return doc;

    }




}

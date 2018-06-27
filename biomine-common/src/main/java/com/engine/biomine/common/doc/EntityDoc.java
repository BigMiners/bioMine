package com.engine.biomine.common.doc;

import com.engine.biomine.common.FIELDS;
import org.apache.solr.common.SolrInputDocument;

/**
 * Created by halmeida on 9/16/16.
 */
public class EntityDoc extends BiomineDoc {

    public EntityDoc() {
        super();
    }

    String pmc;
    String pmid;
    String fungus;
    String enzyme;
    String accessionNumber;
    String glycosation;
    String pH;
    String productAnalysis;
    String temperature;
    String entities;


    @Override
    public SolrInputDocument toSolrDoc() {

        SolrInputDocument doc = new SolrInputDocument();

        if(!isEmpty()) {
            doc.setField(FIELDS.id, getId());
            doc.setField(FIELDS.pmc, getPmc());
            doc.setField(FIELDS.pmid, getPmid());
            doc.setField(FIELDS.fungus, getFungus());
            doc.setField(FIELDS.enzyme, getEnzyme());
            doc.setField(FIELDS.accessionNumber, getAccessionNumber());
            doc.setField(FIELDS.glycosation, getGlycosation());
            doc.setField(FIELDS.pH, getpH());
            doc.setField(FIELDS.productAnalysis, getProductAnalysis());
            doc.setField(FIELDS.temperature, getTemperature());
            doc.setField(FIELDS.entities, getEntities());

        }

        return doc;
    }


    @Override
    public void setField(String field, Object value) {
        switch (field) {
            case (FIELDS.id):
                setId((String) value);
                break;
            case (FIELDS.pmc):
                setPmc((String) value);
                break;
            case (FIELDS.pmid):
                setPmid((String) value);
                break;
            case (FIELDS.fungus):
                setFungus((String) value);
                break;
            case (FIELDS.enzyme):
                setEnzyme((String) value);
                break;
            case (FIELDS.accessionNumber):
                setAccessionNumber((String) value);
                break;
            case (FIELDS.glycosation):
                setGlycosation((String) value);
                break;
            case (FIELDS.pH):
                setpH((String) value);
                break;
            case (FIELDS.productAnalysis):
                setProductAnalysis((String) value);
                break;
            case (FIELDS.temperature):
                setTemperature((String) value);
                break;
            case (FIELDS.entities):
                setEntities((String) value);
        }
    }

    @Override
    public Object getField(String field) {

        switch (field) {
            case (FIELDS.id):
                return (String) getId();
            case (FIELDS.pmc):
                return (String) getPmc();
            case (FIELDS.pmid):
                return (String) getPmid();
            case (FIELDS.fungus):
                return (String) getFungus();
            case (FIELDS.enzyme):
                return (String) getEnzyme();
            case (FIELDS.accessionNumber):
                return (String) getAccessionNumber();
            case (FIELDS.glycosation):
                return (String) getGlycosation();
            case (FIELDS.pH):
                return (String) getpH();
            case (FIELDS.productAnalysis):
                return (String) getProductAnalysis();
            case (FIELDS.temperature):
                return (String) getTemperature();
            case (FIELDS.entities):
                return (String) getEntities();
            default:
                return null;
        }
    }

    @Override
    public boolean isEmpty(){
//        if(fungus.isEmpty() &&
//                enzyme.isEmpty() &&
//                accessionNumber.isEmpty() &&
//                glycosation.isEmpty() &&
//                pH.isEmpty() &&
//                productAnalysis.isEmpty() &&
//                temperature.isEmpty())

            if(fungus == null &&
                    enzyme == null &&
                    accessionNumber == null &&
                    glycosation == null &&
                    pH == null &&
                    productAnalysis == null &&
                    temperature == null)
            return true;
        else return false;
    }

    /**
     * Cleans doc name of extensions,
     * special chars,and adds PID to it
     *
     * @param id
     * @return
     */
    @Override
    public void setId(String id) {
        String name = "";
        if (pmc != null) name += pmc;
        if (pmid != null) name += pmid;

        this.id = getMD5Hash(name);
    }

    public String getEntities() { return entities; }

    public void setEntities(String entities) { this.entities = entities; }

    public String getPmc() {
        return pmc;
    }

    public void setPmc(String pmc) {
        this.pmc = pmc;
    }

    public String getPmid() {
        return pmid;
    }

    public void setPmid(String pmid) {
        this.pmid = pmid;
    }

    public String getFungus() {
        return fungus;
    }

    public void setFungus(String fungus) {
        this.fungus = fungus;
    }

    public String getEnzyme() {
        return enzyme;
    }

    public void setEnzyme(String enzyme) {
        this.enzyme = enzyme;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getGlycosation() {
        return glycosation;
    }

    public void setGlycosation(String glycosation) {
        this.glycosation = glycosation;
    }

    public String getpH() {
        return pH;
    }

    public void setpH(String pH) {
        this.pH = pH;
    }

    public String getProductAnalysis() {
        return productAnalysis;
    }

    public void setProductAnalysis(String productAnalysis) {
        this.productAnalysis = productAnalysis;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

}
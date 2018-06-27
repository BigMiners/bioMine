package com.engine.biomine.common.doc;

import com.engine.biomine.common.FIELDS;
import org.apache.solr.common.SolrInputDocument;

/**
 * Created by halmeida on 8/17/16.
 */
public class AnnotationDoc extends BiomineDoc {

    String proteinId;
    int proteinLength;
    String geneName;
    String description;
    String evidence;
    String evidenceCode;
    String cazy;
    String cbm;
    boolean gpi;
    boolean kdel;
    String wolfPsort;
    int spLength;
    String goFunction;
    String goComponent;
    String goProcess;
    String provenance;

    @Override
    public Object getField(String field) {
        switch(field) {
            case (FIELDS.id):
                return (String) getId();
            case (FIELDS.proteinId):
                return (String) getProteinId();
            case (FIELDS.proteinLength):
                return (int) getProteinLength();
            case (FIELDS.geneName):
                return (String) getGeneName();
            case (FIELDS.description):
                return (String) getDescription();
            case (FIELDS.evidence):
                return (String) getEvidence();
            case (FIELDS.evidenceCode):
                return (String) getEvidenceCode();
            case (FIELDS.cazy):
                return (String) getCazy();
            case (FIELDS.gpi):
                return (boolean) isGpi();
            case (FIELDS.kdel):
                return (boolean) isKdel();
            case (FIELDS.wolfPsort):
                return (String) getWolfPsort();
            case (FIELDS.spLength):
                return (int) getSpLength();
            case (FIELDS.goFunction):
                return (String) getGoFunction();
            case (FIELDS.goComponent):
                return (String) getGoComponent();
            case (FIELDS.goProcess):
                return (String) getGoProcess();
            case (FIELDS.provenance):
                return (String) getProvenance();
            default:
                return "";
        }
    }


    @Override
    public void setField(String field, Object value) {
        switch (field) {
            case (FIELDS.id):
                setId((String) value);
                break;
            case (FIELDS.proteinId):
                setProteinId((String) value);
                break;
            case (FIELDS.proteinLength):
                setProteinLength((int) value);
                break;
            case (FIELDS.geneName):
                setGeneName((String) value);
                break;
            case (FIELDS.description):
                setDescription((String) value);
                break;
            case (FIELDS.evidenceCode):
                setEvidenceCode((String) value);
                break;
            case (FIELDS.evidence):
                setEvidence((String) value);
                break;
            case (FIELDS.cazy):
                setCazy((String) value);
                break;
            case (FIELDS.gpi):
                setGpi((boolean) value);
                break;
            case (FIELDS.kdel):
                setKdel((boolean) value);
                break;
            case (FIELDS.wolfPsort):
                setWolfPsort((String) value);
                break;
            case (FIELDS.spLength):
                setSpLength((int) value);
                break;
            case (FIELDS.goFunction):
                setGoFunction((String) value);
                break;
            case (FIELDS.goProcess):
                setGoProcess((String) value);
                break;
            case (FIELDS.goComponent):
                setGoComponent((String) value);
                break;
            case (FIELDS.provenance):
                setProvenance((String) value);
        }
    }

    @Override
    public SolrInputDocument toSolrDoc() {
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField(FIELDS.id, getId());
        doc.setField(FIELDS.proteinId, getProteinId());
        doc.setField(FIELDS.proteinLength, getProteinLength());
        doc.setField(FIELDS.geneName, getGeneName());
        doc.setField(FIELDS.description, getDescription());
        doc.setField(FIELDS.evidence, getEvidence());
        doc.setField(FIELDS.evidenceCode, getEvidenceCode());
        doc.setField(FIELDS.cazy, getCazy());
        doc.setField(FIELDS.gpi, isGpi());
        doc.setField(FIELDS.kdel, isKdel());
        doc.setField(FIELDS.wolfPsort, getWolfPsort());
        doc.setField(FIELDS.spLength, getSpLength());
        doc.setField(FIELDS.goFunction, getGoFunction());
        doc.setField(FIELDS.goComponent, getGoComponent());
        doc.setField(FIELDS.goProcess, getGoProcess());
        doc.setField(FIELDS.provenance, getProvenance());

        return doc;
    }

    public String getProteinId() {
        return proteinId;
    }

    public void setProteinId(String proteinId) {
        this.proteinId = proteinId;
    }

    public int getProteinLength() {
        return proteinLength;
    }

    public void setProteinLength(int proteinLength) {
        this.proteinLength = proteinLength;
    }

    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    public String getEvidenceCode() {
        return evidenceCode;
    }

    public void setEvidenceCode(String evidenceCode) {
        this.evidenceCode = evidenceCode;
    }

    public String getCazy() {
        return cazy;
    }

    public void setCazy(String cazy) {
        this.cazy = cazy;
    }

    public String getCbm() {
        return cbm;
    }

    public void setCbm(String cbm) {
        this.cbm = cbm;
    }

    public boolean isGpi() {
        return gpi;
    }

    public void setGpi(boolean gpi) {
        this.gpi = gpi;
    }

    public boolean isKdel() {
        return kdel;
    }

    public void setKdel(boolean kdel) {
        this.kdel = kdel;
    }

    public String getWolfPsort() {
        return wolfPsort;
    }

    public void setWolfPsort(String wolfPsort) {
        this.wolfPsort = wolfPsort;
    }

    public int getSpLength() {
        return spLength;
    }

    public void setSpLength(int spLength) {
        this.spLength = spLength;
    }

    public String getGoFunction() {
        return goFunction;
    }

    public void setGoFunction(String goFunction) {
        this.goFunction = goFunction;
    }

    public String getGoProcess() {
        return goProcess;
    }

    public void setGoProcess(String goProcess) {
        this.goProcess = goProcess;
    }

    public String getGoComponent() {
        return goComponent;
    }

    public void setGoComponent(String goComponent) {
        this.goComponent = goComponent;
    }

    public String getProvenance() {
        return provenance;
    }

    public void setProvenance(String provenance) {
        this.provenance = provenance;
    }

}

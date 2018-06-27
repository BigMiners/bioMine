package com.engine.biomine.common.doc;

import com.engine.biomine.common.FIELDS;
import org.apache.solr.common.SolrInputDocument;

/**
 * Created by halmeida on 8/15/16.
 */
public class FastaDoc extends BiomineDoc {

    public static enum Fields{
        attributeId (0),
        geneModelSeq (1),
        proteinSeq (1),
        scaffoldSeq (1),
        provenance (2),
        genomeName (3),
        genomeVersion (4);

        private final int index;
        Fields(int index){
            this.index = index;
        }
        private int index(){ return index;}
    }
    //------ fasta ------//
    String attributeId;
    String geneModelSeq;
    String proteinSeq;
    String scaffoldSeq;
    String provenance;
    String genomeName;
    String genomeVersion;

    @Override
    public Object getField(String field){
        switch(field) {
            case (FIELDS.id):
                return (String) getId();
            case (FIELDS.attributeId):
                return (String) getAttributeId();
            case (FIELDS.geneModelSeq):
                return (String) getGeneModelSeq();
            case (FIELDS.proteinSeq):
                return (String) getProteinSeq();
            case (FIELDS.scaffoldSeq):
                return (String) getScaffoldSeq();
            case (FIELDS.provenance):
                return (String) getProvenance();
            case (FIELDS.genomeName):
                return (String) getGenomeName();
            case (FIELDS.genomeVersion):
                return (String) getGenomeVersion();
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
            case (FIELDS.attributeId):
                setAttributeId((String)value);
                break;
            case (FIELDS.geneModelSeq):
                setGeneModelSeq((String)value);
                break;
            case (FIELDS.proteinSeq):
                setProteinSeq((String)value);
                break;
            case (FIELDS.scaffoldSeq):
                setScaffoldSeq((String)value);
                break;
            case (FIELDS.provenance):
                setProvenance((String)value);
                break;
            case (FIELDS.genomeName):
                setGenomeName((String)value);
                break;
            case (FIELDS.genomeVersion):
                setGenomeVersion((String)value);
                break;
        }
    }

    @Override
    public SolrInputDocument toSolrDoc() {
        SolrInputDocument doc = new SolrInputDocument();

        doc.setField(FIELDS.id, getId());
        doc.setField(FIELDS.attributeId, getAttributeId());
        doc.setField(FIELDS.geneModelSeq, getGeneModelSeq());
        doc.setField(FIELDS.proteinSeq, getProteinSeq());
        doc.setField(FIELDS.scaffoldSeq, getScaffoldSeq());
        doc.setField(FIELDS.provenance, getProvenance());
        doc.setField(FIELDS.genomeName, getGenomeName());
        doc.setField(FIELDS.genomeVersion, getGenomeVersion());


        return doc;
    }

    public String getGenomeVersion() {
        return genomeVersion;
    }

    public void setGenomeVersion(String genomeVersion) {
        this.genomeVersion = genomeVersion;
    }

    public String getGenomeName(){
        return genomeName;
    }

    public void setGenomeName(String genomeName){
        this.genomeName = genomeName;
    }

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public String getGeneModelSeq() {
        return geneModelSeq;
    }

    public void setGeneModelSeq(String geneModelSeq) {
        this.geneModelSeq = geneModelSeq;
    }

    public String getProteinSeq() {
        return proteinSeq;
    }

    public void setProteinSeq(String proteinSeq) {
        this.proteinSeq = proteinSeq;
    }

    public String getScaffoldSeq() {
        return scaffoldSeq;
    }

    public void setScaffoldSeq(String scaffoldSeq) {
        this.scaffoldSeq = scaffoldSeq;
    }

    public String getProvenance() { return provenance; }

    public void setProvenance(String provenance) { this.provenance = provenance; }

    @Override
    public void setId(String id){
        String name = id + "_" + attributeId;
        this.id = getMD5Hash(name);
    }



}

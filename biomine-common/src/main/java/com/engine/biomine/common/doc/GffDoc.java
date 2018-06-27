package com.engine.biomine.common.doc;

import com.engine.biomine.common.FIELDS;
import org.apache.solr.common.SolrInputDocument;

/**
 * Created by halmeida on 8/15/16.
 */
public class GffDoc extends BiomineDoc {


    public static enum Fields{
        correspondingSeq (0),
        featureSource (1),
        featureName (2),
        featureStart (3),
        featureEnd (4),
        score (5),
        strand (6),
        phase (7),
        attributeId (8);
//        provenance (9),
//        genomeName (10),
//        genomeVersion (11);

        private final int index;
        Fields(int index){
            this.index = index;
        }
        private int index(){ return index;}
    }

    //------ gff ------//
    String correspondingSeq;
    String featureSource;
    String featureName;
    int featureStart;
    int featureEnd;
    float score;
    String strand;
    String phase;
    String attributeId;
    String provenance;
    String genomeName;
    String genomeVersion;
    //-----------------//


    @Override
    public Object getField(String field){
        switch(field) {
            case (FIELDS.id):
                return (String) getId();
            case (FIELDS.correspondingSeq):
                return (String) getCorrespondingSeq();
            case (FIELDS.featureSource):
                return (String) getFeatureSource();
            case (FIELDS.featureName):
                return (String) getFeatureName();
            case (FIELDS.featureStart):
                return (int) getFeatureStart();
            case (FIELDS.featureEnd):
                return (int) getFeatureEnd();
            case (FIELDS.score):
                return (float) getScore();                
            case (FIELDS.strand):
                return (String) getStrand();
            case (FIELDS.phase):
                return (String) getPhase();
            case (FIELDS.attributeId):
                return (String) getAttributeId();
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
            case (FIELDS.correspondingSeq):
                setCorrespondingSeq((String) value);
                break;
            case (FIELDS.featureSource):
                setFeatureSource((String) value);
                break;
            case (FIELDS.featureName):
                setFeatureName((String) value);
                break;
            case (FIELDS.featureStart):
                setFeatureStart((int) value);
                break;
            case (FIELDS.featureEnd):
                setFeatureEnd((int) value);
                break;
            case (FIELDS.score):
                setScore((float) value);
                break;
            case (FIELDS.strand):
                setStrand((String) value);
                break;
            case(FIELDS.phase):
                setPhase((String) value);
                break;
            case (FIELDS.attributeId):
                setAttributeId((String) value);
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
        doc.setField(FIELDS.correspondingSeq, getCorrespondingSeq());
        doc.setField(FIELDS.featureSource, getFeatureSource());
        doc.setField(FIELDS.featureName, getFeatureName());
        doc.setField(FIELDS.featureStart, getFeatureStart());
        doc.setField(FIELDS.featureEnd, getFeatureEnd());
        doc.setField(FIELDS.score, getScore());
        doc.setField(FIELDS.strand, getStrand());
        doc.setField(FIELDS.phase, getPhase());
        doc.setField(FIELDS.attributeId, getAttributeId());
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

    public String getFeatureSource() {
        return featureSource;
    }

    public void setFeatureSource(String featureSource) { this.featureSource = featureSource; }

    public String getCorrespondingSeq() { return correspondingSeq;  }

    public void setCorrespondingSeq(String correspondingSeq) {  this.correspondingSeq = correspondingSeq; }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) { this.featureName = featureName;  }

    public int getFeatureStart() {
        return featureStart;
    }

    public void setFeatureStart(int featureStart) {
        this.featureStart = featureStart;
    }

    public int getFeatureEnd() {
        return featureEnd;
    }

    public void setFeatureEnd(int featureEnd) {
        this.featureEnd = featureEnd;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public String getProvenance() { return provenance; }

    public void setProvenance(String provenance) { this.provenance = provenance; }

    @Override
    public void setId(String id){
        String name = id + "_" + attributeId;
        this.id = getMD5Hash(name);
    }
}

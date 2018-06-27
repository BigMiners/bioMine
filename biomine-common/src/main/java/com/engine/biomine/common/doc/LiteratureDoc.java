package com.engine.biomine.common.doc;

import com.engine.biomine.common.FIELDS;
import org.apache.solr.common.SolrInputDocument;
import java.io.UnsupportedEncodingException;

/**
 * Created by halmeida on 8/15/16.
 */

public class LiteratureDoc extends BiomineDoc {

    public LiteratureDoc(){
        super();
    }

//    public enum FIELDS{
//        ;
//        private String value;
//        FIELDS(final String value) {       this.value = value;        }
//        public String getValue() {           return value;        }
//        @Override
//        public String toString() {      return this.getValue();       }
//    }

    String abs;
    String body;
    String journalTitle;
    String pmid;
    String pmc;
    String title;
    Author[] authors;
    String[] keywords;
    String[] meshTerms;
    Reference[] references;
    int year;
    String captions;


    @Override
    public SolrInputDocument toSolrDoc() {
        SolrInputDocument doc = new SolrInputDocument();

        doc.setField(FIELDS.id, getId());
        doc.setField(FIELDS.abs, getAbs());
        doc.setField(FIELDS.body, getBody());
        doc.setField(FIELDS.journalTitle, getJournalTitle());
        doc.setField(FIELDS.pmid, getPmid());
        doc.setField(FIELDS.pmc, getPmc());
        doc.setField(FIELDS.articleTitle, getTitle());
        doc.setField(FIELDS.captions, getCaptions());

        Author[] authors = getAuthors();
        if (authors != null && authors.length > 0) {
            for (Author author : authors) {
                String authorFullName = author.getName() + " " + author.getSurname();
                doc.addField(FIELDS.author, authorFullName);
            }
        }

        String[] keywords = getKeywords();
        if (keywords != null && keywords.length > 0) {
            for (String keyword : keywords) {
                doc.addField(FIELDS.keywords, keyword);
            }
        }

        if (getReferences() != null) {
            for (Reference ref : getReferences()) {
                doc.addField(FIELDS.referenceTitle, ref.getTitle());
                doc.addField(FIELDS.referenceId, ref.getPubId());
                for (Author author : ref.getAuthors()) {
                    String authorFullName = author.getName() + " " + author.getSurname();
                    doc.addField(FIELDS.referenceAuthor, authorFullName);
                }
            }
        }

        if (getYear() > 0) {
            doc.addField(FIELDS.year, getYear());
        }

        return doc;
    }

    @Override
    public void setField(String field, Object value){
        switch(field) {
            case (FIELDS.id):
                setId((String) value);
                break;
            case (FIELDS.abs):
                setAbs((String) value);
                break;
            case (FIELDS.body):
                setBody((String) value);
                break;
            case (FIELDS.journalTitle):
                setJournalTitle((String) value);
                break;
            case (FIELDS.pmc):
                setPmc((String) value);
                break;
            case (FIELDS.pmid):
                setPmid((String) value);
                break;
            case (FIELDS.articleTitle):
                setTitle((String) value);
                break;
            case (FIELDS.author):
                setAuthors((Author[]) value);
                break;
            case (FIELDS.keywords):
                setKeywords((String[]) value);
                break;
            case(FIELDS.meshTerms):
                setMeshTerms((String[]) value);
                break;
            case (FIELDS.year):
                setYear((int) value);
                break;
            case (FIELDS.captions):
                setCaptions((String)value);
                break;

        }
    }

    @Override
    public Object getField(String field){
        
        switch(field) {
            case (FIELDS.id):
                return (String) getId();
            case (FIELDS.abs):
                return (String) getAbs();
            case (FIELDS.author):
                return (Author[]) getAuthors();
            case (FIELDS.articleTitle):
                return (String) getTitle();
            case (FIELDS.body):
                return (String) getBody();
            case (FIELDS.journalTitle):
                return (String) getJournalTitle();
            case (FIELDS.pmc):
                return (String) getPmc();
            case (FIELDS.pmid):
                return (String) getPmid();
            case (FIELDS.keywords):
                return (String[]) getKeywords();
            case (FIELDS.meshTerms):
                return (String[]) getMeshTerms();
            case (FIELDS.year):
                return (int) getYear();
            case (FIELDS.captions):
                return (String) getCaptions();
            default:
                return "";
        }
    }

    /**
     * Cleans doc name of extensions,
     * special chars,and adds PID to it
     * @param id
     * @return
     */
    @Override
    public void setId(String id){
        String name = journalTitle + title;
        this.id = getMD5Hash(name);
    }

    public String getAbs() {
        return abs;
    }

    public void setAbs(String abs) {
        this.abs = abs;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getJournalTitle() {
        return journalTitle;
    }

    public void setJournalTitle(String journalTitle) {
        this.journalTitle = journalTitle;
    }

    public String getPmid() {
        return pmid;
    }

    public void setPmid(String pmid) {
        this.pmid = pmid;
    }

    public String getPmc() {
        return pmc;
    }

    public void setPmc(String pmc) {
        this.pmc = pmc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Author[] getAuthors() {
        return authors;
    }

    public void setAuthors(Author[] authors) {
        this.authors = authors;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    public String[] getMeshTerms() { return meshTerms; }

    public void setMeshTerms(String[] meshTerms) { this.meshTerms = meshTerms; }

    public Reference[] getReferences() {
        return references;
    }

    public void setReferences(Reference[] references) {
        this.references = references;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getCaptions(){
        return captions;
    }

    public void setCaptions(String captions) {
        this.captions = captions;

    }

    @Override
    public String toString(){
        String artDoc = "";

        int sizeBody = 0;
        int sizeAbs = 0;

        try {
            sizeBody = getBody().getBytes("UTF8").length;
            sizeAbs = getAbs().getBytes().length;

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        artDoc = "Document: " + getId() + "\n"
                + "body size: " + sizeBody + "\n"
                + "abs size: " + sizeAbs + "\n"
                + "mesh size: " + meshTerms.length + "\n"
                + getJournalTitle() + "\n"
                + getTitle() + "\n"
                + getPmc() + "\t" + getPmid();

        return artDoc;
    }

    

}

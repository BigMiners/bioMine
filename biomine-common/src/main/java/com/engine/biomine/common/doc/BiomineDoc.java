/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.engine.biomine.common.doc;

import com.engine.biomine.common.FIELDS;
import org.apache.solr.common.SolrInputDocument;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;


/**
 *
 * @author ludovic
 */
public class BiomineDoc {

    String id;

    public BiomineDoc() {

    }

    //public enum FIELDS{}

    /**
     * Populates a Solr doc with
     * content in bioMine doc fields
     *
     * @return
     */
    public SolrInputDocument toSolrDoc(){
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField(FIELDS.id, getId());
        return doc;
    }

    /**
     * Generates MD5 hash for contents
     * Used for generating bioMine IDs
     *
     * @param name
     * @return
     */
    public String getMD5Hash(String name){

        try {
            byte[] byteid = name.getBytes("UTF-8");
            name = DatatypeConverter.printHexBinary(MessageDigest.getInstance("MD5").digest(byteid));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if(name.length() > 30)
            name = name.substring(0,30);

        return name;
    }

    public boolean isEmpty(){
        if(id== null || id.isEmpty())
            return true;
        else return false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setField(String field, Object value){}

    public Object getField(String field){ return new Object(); }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BiomineDoc other = (BiomineDoc) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    
    
    
}

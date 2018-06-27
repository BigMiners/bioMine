/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.engine.biomine.query.result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author ludovic
 */
public class DocumentResult implements Serializable {

//    public ArrayList<Field> fields;
    public HashMap<String,String> fields;
    public Map<String, List<String>> highlightSnippets;
//    public Map<String, List<String>> highlightTerms;
    private Pattern highlightPattern;
    //TODO: move to config files
    private String highlightStarTag = "<em>";
    private String highlightEndTag = "</em>";

    public DocumentResult() {
//        this.fields = new ArrayList<>();
        this.fields = new HashMap<>();
        this.highlightSnippets = new HashMap<>();
//        this.highlightTerms = new HashMap<>();
        this.highlightPattern = Pattern.compile(highlightStarTag + "(.+?)" + highlightEndTag);
    }

//    public DocumentResult(ArrayList<Field> fields, double score) {
//        this.fields = fields;
//    }
//    public ArrayList<Field> getFields() {
    public HashMap<String,String> getFields(){
        return fields;
    }
//    public void setFields(ArrayList<Field> fields) {
    public void setFields(HashMap<String,String> fields){
        this.fields = fields;
    }
    public void addField(String fieldLabel, Object value) {
        this.fields.put(fieldLabel, String.valueOf(value));
    }

    public void addFields(ArrayList<String> docFields){

    }

    public Map<String, List<String>> getHighlightSnippets() {
        return highlightSnippets;
    }

    public void setHighlightSnippets(Map<String, List<String>> highlightSnippets) {
        this.highlightSnippets = highlightSnippets;
    }

//    public Map<String, List<String>> getHighlightTerms() {
//        return highlightTerms;
//    }
//
//    public void setHighlightTerms(Map<String, List<String>> highlightTerms) {
//        this.highlightTerms = highlightTerms;
//    }

//    public void addHighlightTerms(Map<String, List<String>> highlightSnippets) {
//
//        for (Entry highlight : highlightSnippets.entrySet()) {
//            String fieldName = highlight.getKey().toString();
//            List<String> snippets = (List<String>) highlight.getValue();
//
//            Set<String> hitTerms = new HashSet<>();
//
//            for (String snippet : snippets) {
//                Matcher matcher = highlightPattern.matcher(snippet);
//                while (matcher.find()) {
//                    hitTerms.add(matcher.group(1));
//                }
//            }
//
//            highlightTerms.put(fieldName, new ArrayList(hitTerms));
//
//        }
//
//    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.engine.biomine.query.result;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author ludovic
 */
public class SearchResponse implements Serializable {

    long totalItemFound;
    int numItemPerPage;
    List<DocumentResult> items;
//    List<FacetResult> facets;

    public SearchResponse() {
    }

    public SearchResponse(List<DocumentResult> items, int totalItemFound, int numItemPerPage) {
        this.totalItemFound = totalItemFound;
        this.numItemPerPage = numItemPerPage;
        this.items = items;
    }

    public long getTotalItemFound() {
        return totalItemFound;
    }

    public void setTotalItemFound(long totalItemFound) {
        this.totalItemFound = totalItemFound;
    }

    public int getNumItemPerPage() {
        return numItemPerPage;
    }

    public void setNumItemPerPage(int numItemPerPage) {
        this.numItemPerPage = numItemPerPage;
    }

    public List<DocumentResult> getItems() {
        return items;
    }

    public void setItems(List<DocumentResult> items) {
        this.items = items;
    }



//    public List<FacetResult> getFacets() {
//        return facets;
//    }
//
//    public void setFacets(List<FacetResult> facets) {
//        this.facets = facets;
//    }

    
}

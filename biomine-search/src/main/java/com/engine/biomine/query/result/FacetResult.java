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
public class FacetResult implements Serializable {

    String category;
    List<FacetItem> facetItem;
    
    public FacetResult(){}
    
    public FacetResult(String category, List<FacetItem> facetItem){
        this.category = category;
        this.facetItem = facetItem;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<FacetItem> getFacetItem() {
        return facetItem;
    }

    public void setFacetItem(List<FacetItem> facetItem) {
        this.facetItem = facetItem;
    }

    

}

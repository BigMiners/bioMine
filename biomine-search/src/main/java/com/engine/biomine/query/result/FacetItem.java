/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.engine.biomine.query.result;

import java.io.Serializable;

/**
 *
 * @author ludovic
 */
public class FacetItem implements Serializable{

        String label;
        long value;
        
        public FacetItem(){}
        
        public FacetItem(String label, long value){
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }

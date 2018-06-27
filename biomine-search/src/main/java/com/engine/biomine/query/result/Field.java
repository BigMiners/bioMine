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
public class Field implements Serializable{

        public String label;
        public Object value;
        
        public Field() {}

        public Field(String label, Object value) {
            this.label = label;
            this.value = value;
        }

        @Override
        public String toString(){
            return this.label + " : "+ this.value;
        }
        

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }

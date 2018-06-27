/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.engine.biomine.evaluation.response;

import java.util.HashMap;

/**
 *
 * @author ludovic
 */
public class EvalResponse {

    double mrr;
    int nbQueries;
    int nbMissed;
//    String
    HashMap<String,Double> RRscorePerQuery;

    public EvalResponse(){}

    public EvalResponse(int nbQueries, int nbMissed, HashMap<String,Double> RRscorePerQuery, double mrr) {
        this.nbQueries = nbQueries;
        this.nbMissed = nbMissed;
        this.mrr = mrr;
        this.RRscorePerQuery = RRscorePerQuery;
    }


    public double getMrr() {
        return mrr;
    }

    public void setRr(double rr) {
         mrr = rr;
    }

    public int getNbMissed(){
        return nbMissed;
    }

    public void setNbMissed(int nbMissed){
        this.nbMissed = nbMissed;
    }

    public HashMap<String,Double> getRRscorePerQuery(){
        return RRscorePerQuery;
    }

    public void setRRscorePerQuery(HashMap<String,Double> RRscorePerQuery){
        this.RRscorePerQuery = RRscorePerQuery;
    }
    public int getNbQueries() {
        return nbQueries;
    }

    public void setNbQueries(int nbQueries) {
        this.nbQueries = nbQueries;
    }

    
}

package com.engine.biomine.evaluation;

import com.engine.biomine.common.IOUtil;
import com.engine.biomine.query.QueryManager;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by halmeida on 12/17/15.
 */
public class GoldStandard {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private HashMap<String,String> goldQueries;
    private HashMap<String,String> goldIDs;
    private HashMap<String,Double> goldRRMapping = new HashMap<>();


    public GoldStandard(){
        goldQueries = new HashMap<String, String>();
        goldIDs = new HashMap<>();
//        this.evalPath = conf.getProps().getProperty("eval.path");
//        this.evalFiles = StringUtils.split(conf.getProps().getProperty("eval.files"), ",");
    }

    public void loadGoldStd(String queries_path, String qrels_path){
        goldQueries.putAll(IOUtil.getINSTANCE().loadTabMapFile(queries_path));
        goldIDs.putAll(IOUtil.getINSTANCE().loadTabMapFile(qrels_path));
    }

    public int getNumberOfQueries(){
        return goldQueries.size();
    }
    public ArrayList<Double> getGoldRRScores(){
        return new ArrayList<Double>(goldRRMapping.values());
    }

    public HashMap<String,Double> getGoldRRMapping(){
        return goldRRMapping;
    }

    public int getNumberMissed(){
        int missed = 0;
        for(Double score : goldRRMapping.values()){
            if(score == 0.0)
                missed++;
        }
        return missed;
    }

    public void computeGoldRRscores(QueryManager manager, Evaluator eval, int startOffset, int nbResult, boolean expansion, boolean highlight){
        String task =  "eval";
        Iterator<String> iter = goldQueries.keySet().iterator();

        while(iter.hasNext()){
            String key = iter.next();
            String evalQuery = goldQueries.get(key);
            QueryResponse response = manager.processQuery(evalQuery, task, startOffset, nbResult, expansion, highlight);

            double rr = eval.computeReciprocalRank(evalQuery, response.getResults(), getGoldForQuery(evalQuery));
            goldRRMapping.put(evalQuery,rr);
        }
    }

    public HashMap<String,String> getGoldQueries(){
        return goldQueries;
    }

    public HashMap<String,String> getGoldIDs(){
        return goldIDs;
    }

    public List<String> getGoldForQuery(String query){

        String[] goldForQuery = null;

        try {
            for (Map.Entry<String, String> entry : goldQueries.entrySet()) {
                if (entry.getValue().contains(query)) {
                    String queryID = entry.getKey();
                    goldForQuery = StringUtils.split(goldIDs.get(queryID), ",");
                }
            }
        }catch (NullPointerException e){
            logger.error("User query not in evaluation data. ", e);
        }
        return Arrays.asList(goldForQuery);
    }




//    private void loadEvaluationData(){
//
//        for(int i = 0; i < evalFiles.length; i++){
//            String path = evalPath;
//            String file = evalFiles[i];
//
//            goldQueries.putAll(IOutil.loadTabMapFile(path + "/" +  file + "_queries.txt"));
//            goldIDs.putAll(IOutil.loadTabMapFile(path + "/" + file + "_qrels.txt"));
//        }
//
////        logger.info("Evaluation data loaded: " + goldQueries.size() + " evaluation queries and " + goldIDs.size() + "queries mapped to PIDs.");
//
//    }

}

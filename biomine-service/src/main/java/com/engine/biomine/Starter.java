package com.engine.biomine;

//import com.engine.biomine.evaluation.Evaluator;
import com.engine.biomine.common.Configs;
import com.engine.biomine.evaluation.Evaluator;
import com.engine.biomine.indexing.IndexManager;
//import com.engine.biomine.query.QueryManager;

import com.engine.biomine.query.QueryManager;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

@Deprecated
public class Starter {

    private final static Logger logger = LoggerFactory.getLogger(Starter.class);
    public static Scanner input = new Scanner(System.in);

    public void main(String[] args) {
//  public static void main(String[] args) {

    	    	//load properties
        Properties props = Configs.getInstance().getProps();

        String task = props.getProperty("bioMine.task");
		boolean isEval = Boolean.valueOf(props.getProperty("run.eval"));

        if(task.contains("index")){
        	//create
        	IndexManager indexer = new IndexManager();
        	String dataPath = props.getProperty("data.path");
        	logger.info("Indexing data from path {}", dataPath);
//        	indexer.pushData();
        	logger.info("Indexing finished");
        	//System.exit(-1);
        }

        else if(task.contains("query")){
        	//query
        	QueryManager queryManager = new QueryManager();
        	Evaluator eval = new Evaluator();
        	logger.info("== Starting bioMine Search ==");
        	logger.info("Please provide query (or ENTER to exit): ");
        	String query = input.nextLine();

			List<Double> MRRScore = new ArrayList<Double>();
        	int numberOfQueries = 0;

        	//user input for query
        	while(!query.isEmpty())  {

				numberOfQueries++;
				QueryResponse response = queryManager.processQuery(query, task, 0, 0, false, false);

				//compute RR for gold query if eval is run

				double score = eval.computeReciprocalRank(query, response.getResults(), null);
				//have to find another way of doing this
//				eval.compareRanking(query);
				MRRScore.add(score);

				//output just IDs for query, in case no eval is run
//				eval.outputIDResults(response.getResults());

        		logger.info("\nPlease provide query (or ENTER to exit): ");
        		query = input.nextLine();
        	}

			logger.info("Number of queries -> " + numberOfQueries);
			eval.computeMRRScore(MRRScore);

        	logger.info("Query processing finished.");
        }
		else if(task.contains("annotate")){

		}

        System.exit(1);
    }

}

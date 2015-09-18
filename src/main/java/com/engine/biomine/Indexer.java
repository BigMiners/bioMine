package com.engine.biomine;

import com.engine.biomine.indexing.Configs;
import com.engine.biomine.indexing.IndexManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Indexer {
	
	 private final static Logger logger = LoggerFactory.getLogger(Indexer.class);

	    public static void main(String[] args) {
	        
	    	//load properties
	        Configs config = new Configs();
	        
	        //create
	        IndexManager indexer = new IndexManager(config);  
	        String dataPath = config.getProps().getProperty("data.path");
	        logger.info("Indexing data from path {}", dataPath);
	        indexer.pushData();
	        logger.info("Indexing finished");
	        System.exit(-1);
	    }

}

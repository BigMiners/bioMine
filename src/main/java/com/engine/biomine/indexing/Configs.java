package com.engine.biomine.indexing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configs {
	
	Properties props;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final String configFileParam = "index.config";

    public Configs() {
        this.props = new Properties();
        if (System.getProperty(configFileParam) == null) {
        	logger.error("You must set the variable 'index.config', java -Dindex.config=... -jar ...");
            System.exit(-1);
        }
        String configFile = System.getProperty(configFileParam);
        try {
            this.props.load(new FileInputStream(new File(configFile)));
        } catch (IOException ex) {
        	logger.error("Could not load resources {}", configFile, ex);
        }
    }

    public Properties getProps() {
        return props;
    }
}

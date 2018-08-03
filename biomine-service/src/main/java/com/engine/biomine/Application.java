/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.engine.biomine;

import com.engine.biomine.common.Configs;
import com.engine.biomine.swagger.ServletFilter;
import com.engine.biomine.swagger.SwaggerConfig;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 *
 * @author ludovic
 */

@EnableAutoConfiguration(exclude = {SolrAutoConfiguration.class})
@ComponentScan(basePackageClasses = BiomineController.class)
@Import({SwaggerConfig.class, ServletFilter.class})
public class Application {

    private final static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
            	//load properties
        Configs config = new Configs();
        Class<?>[] cl = {Application.class, SwaggerConfig.class, ServletFilter.class};
        SpringApplication.run(cl,args);
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.engine.biomine.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 *
 * @author ludovic
 */
@Configuration
//@EnableWebMvc
@EnableSwagger2 //Loads the spring beans required by the framework
public class SwaggerConfig {

    @Bean
    ApiInfo apiInfo() {
        String description = "bioMine Service";
        String title = "bioMine API";
        String version = "v1.0.0";
        String termsOfService = "";
//        String contactName = "meurs.marie-jean@uqam.ca";
        String contactName = "Almeida H., Jean-Louis L., Meurs M-J. ";
        String licenseUrl = "https://github.com/BigMiners/bioMine/blob/master/LICENSE";
        String licenseName = "MIT License";

        ApiInfo apiInfo = new ApiInfo(title, description, version, termsOfService,
                contactName, licenseName, licenseUrl);
        return apiInfo;
    }
    
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

//    /**
//     * The uiConfig method for SwaggerConfig. See the SWAGGER documentation.
//     * @return UiConfiguration
//     */
//    @Bean
//    public UiConfiguration uiConfig() {
//        return new UiConfiguration(
//                "validatorUrl",// url
//                "none", // docExpansion          => none | list
//                "alpha", // apiSorter             => alpha
//                "schema", // defaultModelRendering => schema
//                UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS,
//                false, // enableJsonEditor      => true | false
//                true, // showRequestHeaders    => true | false
//                60000L);      // requestTimeout => in millisecond
//        // defaults to null (uses jquery xh timeout)
//    }

}

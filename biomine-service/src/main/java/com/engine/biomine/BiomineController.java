/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.engine.biomine;

import com.engine.biomine.common.IOUtil;
import com.engine.biomine.evaluation.Evaluator;
import com.engine.biomine.evaluation.GoldStandard;
import com.engine.biomine.evaluation.response.EvalResponse;
import com.engine.biomine.indexing.IndexManager;
import com.engine.biomine.indexing.IndexerStatus;
import com.engine.biomine.query.QueryManager;
import com.engine.biomine.response.BiomineError;
import com.engine.biomine.query.result.SearchResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.*;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author ludovic
 */
@RestController
@RequestMapping("biomine")
@Api(value = "biomine")
public class BiomineController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    //index manager
    private final IndexManager indexer;
    //query manager
    private final QueryManager queryManager;
    final String collections = "literature, gff, fasta, annotation, entity";

    boolean deleteFile;
//    //evaluation
//    private final Evaluator eval;
//    private final GoldStandard goldStd;

    private String task;

    public BiomineController() throws Exception {
        indexer = new IndexManager();
        queryManager = new QueryManager();
    }

    @ApiOperation(
            // Populates the "summary"
            value = "Index a doc (from local)",
            // Populates the "description"
            notes = "Add a single xml doc to the index"
    )
    @ApiResponses(
            value = {
                @ApiResponse(code = 200, message = "Accepted", response = Boolean.class),
                @ApiResponse(code = 400, message = "Bad Request", response = BiomineError.class),
                @ApiResponse(code = 500, message = "Internal Server Error", response = BiomineError.class),
                @ApiResponse(code = 503, message = "Service Unavailable", response = BiomineError.class)
            }
    )
    @RequestMapping(value="/indexer/index/documents/{doc}", method=RequestMethod.POST, produces = {APPLICATION_JSON_VALUE})
    @ResponseBody
    @ConfigurationProperties()
    public ResponseEntity<Boolean>indexDocument(
            HttpServletResponse response,
            @ApiParam(value = "File path", required = true)
            @RequestParam(value = "path", required = true) MultipartFile path,
            @ApiParam(value= "Collection", required = true)
            @RequestParam(value = "collection", required = true) String collection)
    {
        if (!path.isEmpty()) {
            if (IOUtil.getINSTANCE().isValidExtension(path.getOriginalFilename())) {
                logger.info("Start indexing data from path {}", path.getOriginalFilename());
                try {
                    File file = new File(path.getOriginalFilename());
                    file.createNewFile();
                    deleteFile = true;

                    FileOutputStream output = new FileOutputStream(file);
                    output.write(path.getBytes());
                    output.close();

                    indexer.pushData(file, deleteFile, collection);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else logger.info("File extension not valid. Please select a valid file.");
        }
        else logger.info("File does not exist. Please select a valid file.");
        return new ResponseEntity<Boolean>(true, HttpStatus.OK);
    }


    @ApiOperation(
            // Populates the "summary"
            value = "Index biodata (from local)",
            // Populates the "description"
            notes = "Add a biodata file to the index"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Accepted", response = Boolean.class),
                    @ApiResponse(code = 400, message = "Bad Request", response = BiomineError.class),
                    @ApiResponse(code = 500, message = "Internal Server Error", response = BiomineError.class),
                    @ApiResponse(code = 503, message = "Service Unavailable", response = BiomineError.class)
            }
    )
    @RequestMapping(value="/indexer/index/biodata/{data}", method=RequestMethod.POST, produces = {APPLICATION_JSON_VALUE})
    @ResponseBody
    @ConfigurationProperties()
    public ResponseEntity<Boolean>indexData(
            HttpServletResponse response,
            @ApiParam(value = "File", required = true)
            @RequestParam(value = "path", required = true) MultipartFile path,
            @ApiParam(value= "Collection", required = true)
            @RequestParam(value = "collection", required = true) String collection)
    {
        if (!path.isEmpty()) {
            if (IOUtil.getINSTANCE().isValidExtension(path.getOriginalFilename())) {
                logger.info("Start indexing data from path {}", path.getOriginalFilename());
                try {
                    File file = new File(path.getOriginalFilename());
                    file.createNewFile();
                    deleteFile = false;

                    FileOutputStream output = new FileOutputStream(file);
                    output.write(path.getBytes());
                    output.close();

                    indexer.pushData(file, deleteFile, collection);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else logger.info("File extension not valid. Please select a valid file.");
        }
        else logger.info("File does not exist. Please select a valid file.");
        return new ResponseEntity<Boolean>(true, HttpStatus.OK);
    }


    @ApiOperation(
            // Populates the "summary"
            value = "Index a doc from (remote) path",
            // Populates the "description"
            notes = "Add a single xml doc to the index"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Accepted", response = Boolean.class),
                    @ApiResponse(code = 400, message = "Bad Request", response = BiomineError.class),
                    @ApiResponse(code = 500, message = "Internal Server Error", response = BiomineError.class),
                    @ApiResponse(code = 503, message = "Service Unavailable", response = BiomineError.class)
            }
    )
    @RequestMapping(value = "/indexer/index/path/{doc}", method = RequestMethod.POST, produces = {APPLICATION_JSON_VALUE})
    @ResponseBody
    public ResponseEntity<Boolean> indexDocument(
            @ApiParam(value = "File path", required = true)
            @RequestParam(value = "path", required = true) String path,
            @ApiParam(value = "Collection", required = true, defaultValue = "", allowableValues = collections)
            @RequestParam(value = "collection", required = false) String collection)
    {
        if (!path.isEmpty()) {
            File file = new File(path);
                if (file.isDirectory() || IOUtil.getINSTANCE().isValidExtension(path)) {
                    logger.info("Start indexing data from path {}", path);
                    deleteFile = false;
                    indexer.pushData(file, deleteFile, collection);
                }

        }
        else logger.info("File not valid. Please select a valid file.");
        return new ResponseEntity<Boolean>(true, HttpStatus.OK);
    }


    @ApiOperation(
            // Populates the "summary"
            value = "Index multiple documents (from local)",
            // Populates the "description"
            notes = "Add multiple documents to the index (should be compressed in an archive file)"
    )
    @ApiResponses(
            value = {
                @ApiResponse(code = 200, message = "Accepted", response = Boolean.class),
                @ApiResponse(code = 400, message = "Bad Request", response = BiomineError.class),
                @ApiResponse(code = 500, message = "Internal Server Error", response = BiomineError.class),
                @ApiResponse(code = 503, message = "Service Unavailable", response = BiomineError.class)
            }
    )
    @RequestMapping(value = "/indexer/index/documents", method = RequestMethod.POST, produces = {APPLICATION_JSON_VALUE})
    @ResponseBody
    public ResponseEntity<Boolean> indexDocuments(
            @ApiParam(value = "File path", required = true)
            @RequestParam(value = "path", required = true) MultipartFile path,
            @ApiParam(value= "Collection", required = true)
            @RequestParam(value = "collection", required = true) String collection)
    {
        if (!path.isEmpty()) {
            if (IOUtil.getINSTANCE().isValidExtension(path.getOriginalFilename())) {
                logger.info("Start indexing data from path {}", path.getOriginalFilename());
                try {
                    File file = new File(path.getOriginalFilename());
                    file.createNewFile();

                    FileOutputStream output = new FileOutputStream(file);
                    output.write(path.getBytes());
                    output.close();

                    indexer.pushData(file, deleteFile, collection);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else logger.info("File extension not valid. Please select a valid file.");
        }
        else logger.info("File does not exist. Please select a valid file.");
        return new ResponseEntity<Boolean>(true, HttpStatus.OK);
    }

    @ApiOperation(
            // Populates the "summary"
            value = "Delete a doc",
            // Populates the "description"
            notes = "Remove a doc from the index"
    )
    @ApiResponses(
            value = {
                @ApiResponse(code = 200, message = "Accepted", response = Boolean.class),
                @ApiResponse(code = 400, message = "Bad Request", response = BiomineError.class),
                @ApiResponse(code = 500, message = "Internal Server Error", response = BiomineError.class),
                @ApiResponse(code = 503, message = "Service Unavailable", response = BiomineError.class)
            }
    )
    @RequestMapping(value = "/indexer/index/documents/{doc}", method = RequestMethod.DELETE, produces = {APPLICATION_JSON_VALUE})
    @ResponseBody
    public ResponseEntity<Boolean> deleteDocument(
            @ApiParam(value = "Document id", required = true)
            @RequestParam(value = "docId", required = true) String docId,
            @ApiParam(value= "Collection", required = true)
            @RequestParam(value = "collection", required = true) String collection)
    {
        logger.info("Removing doc (docId: {})from the index", docId);
        boolean res = indexer.deleteDocs(collection, docId);
        return new ResponseEntity<Boolean>(res, HttpStatus.OK);
    }

    @ApiOperation(
            // Populates the "summary"
            value = "Delete documents",
            // Populates the "description"
            notes = "Remove a doc from the index"
    )
    @ApiResponses(
            value = {
                @ApiResponse(code = 200, message = "Accepted", response = Boolean.class),
                @ApiResponse(code = 400, message = "Bad Request", response = BiomineError.class),
                @ApiResponse(code = 500, message = "Internal Server Error", response = BiomineError.class),
                @ApiResponse(code = 503, message = "Service Unavailable", response = BiomineError.class)
            }
    )
    @RequestMapping(value = "/indexer/index/documents", method = RequestMethod.DELETE, produces = {APPLICATION_JSON_VALUE})
    @ResponseBody
    public ResponseEntity<Boolean> deleteDocuments(
            @ApiParam(value = "Document ids", required = true)
            @RequestParam(value = "docIds", required = true) String[] docIds,
            @ApiParam(value= "Collection", required = true)
            @RequestParam(value = "collection", required = true) String collection)
    {
        logger.info("Start removing {} documents from the index", docIds.length);
        boolean res = indexer.deleteDocs(collection, docIds);
        return new ResponseEntity<Boolean>(res, HttpStatus.OK);
    }

    @ApiOperation(
            // Populates the "summary"
            value = "Indexing status",
            // Populates the "description"
            notes = "Get information on the indexing status"
    )
    @ApiResponses(
            value = {
                @ApiResponse(code = 200, message = "Accepted", response = IndexerStatus.class),
                @ApiResponse(code = 400, message = "Bad Request", response = BiomineError.class),
                @ApiResponse(code = 500, message = "Internal Server Error", response = BiomineError.class),
                @ApiResponse(code = 503, message = "Service Unavailable", response = BiomineError.class)
            }
    )
    @RequestMapping(value = "/indexer/index/status", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE})
    @ResponseBody
    public ResponseEntity<IndexerStatus> getStatus() {
        IndexerStatus status = indexer.getStatus();
        return new ResponseEntity<IndexerStatus>(status, HttpStatus.OK);
    }

    @ApiOperation(
            // Populates the "summary"
            value = "Search documents",
            // Populates the "description"
            notes = "Query the index for documents"
    )
    @ApiResponses(
            value = {
                @ApiResponse(code = 200, message = "Accepted", response = SearchResponse.class),
                @ApiResponse(code = 400, message = "Bad Request", response = BiomineError.class),
                @ApiResponse(code = 500, message = "Internal Server Error", response = BiomineError.class),
                @ApiResponse(code = 503, message = "Service Unavailable", response = BiomineError.class)
            }
    )
    @RequestMapping(value = "/searcher/search", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE})
    @ResponseBody
    public ResponseEntity<SearchResponse> searchDocuments(

            @ApiParam(value = "Query", required = true, defaultValue = "*:*")
            @RequestParam(value = "query", required = true) String query,
            @ApiParam(value = "Highlight results", required = false, defaultValue = "false")
            @RequestParam(value = "highlight", required = false) boolean highlightResults,
            @ApiParam(value = "Start offset", required = false, defaultValue = "0")
            @RequestParam(value = "start", required = false) Integer startOffset,
            @ApiParam(value = "Number of results", required = false, defaultValue = "20")
            @RequestParam(value = "nbResult", required = false) Integer numResult,
            @ApiParam(value = "Document fields (no spaces, separated by ',')", required = false, defaultValue = "")
            @RequestParam(value = "fields", required = false) String userFields,
            @ApiParam(value = "Expansion (UMLS terms)", required = false, defaultValue = "false")
            @RequestParam(value = "expandUMLS", required = false) boolean queryExpansion,
            @ApiParam(value = "Collection", required = true, defaultValue = "", allowableValues = collections)
            @RequestParam(value = "collection", required = false) String collection)
    {

        task = "query";
        List<String> fields = Arrays.asList(StringUtils.split(userFields,","));

        QueryResponse qResponse = queryManager.processQuery(query, task, startOffset, numResult, queryExpansion, highlightResults);
        SearchResponse response = queryManager.buildSearchResponse(qResponse, fields, highlightResults);

        return new ResponseEntity<SearchResponse>(response, HttpStatus.OK);
    }

    @ApiOperation(
            // Populates the "summary"
            value = "Evaluate search",
            // Populates the "description"
            notes = "Evaluate doc retrieval"
    )
    @ApiResponses(
            value = {
                @ApiResponse(code = 200, message = "Accepted", response = EvalResponse.class),
                @ApiResponse(code = 400, message = "Bad Request", response = BiomineError.class),
                @ApiResponse(code = 500, message = "Internal Server Error", response = BiomineError.class),
                @ApiResponse(code = 503, message = "Service Unavailable", response = BiomineError.class)
            }
    )
    @RequestMapping(value = "/evaluation/eval", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE})
    @ResponseBody
    public ResponseEntity<EvalResponse> evalSearch(
            @ApiParam(value = "Queries path", required = true)
            @RequestParam(value = "query_path", required = true) String query_path,
            @ApiParam(value = "Qrels path", required = true)
            @RequestParam(value = "qrels_path", required = true) String qrels_path,
            @ApiParam(value = "Start offset", required = false, defaultValue = "0")
            @RequestParam(value = "start", required = false) Integer startOffset,
            @ApiParam(value = "Nb Results", required = false, defaultValue = "20")
            @RequestParam(value = "nbResult", required = false) Integer numResult,
            @ApiParam(value = "Expansion (UMLS terms)", required = false, defaultValue = "false")
            @RequestParam(value = "expandUMLS", required = false) boolean queryExpansion){

        double mrrScore = 0.0;
        //objects should be instantiated here
        //otherwise different Evaluations are put together
        Evaluator eval = new Evaluator();
        GoldStandard goldStd = new GoldStandard();

        //load gold query inputs
        goldStd.loadGoldStd(query_path, qrels_path);
        int numberOfQueries = goldStd.getNumberOfQueries();
        //compute scores for gold queries
        goldStd.computeGoldRRscores(queryManager, eval, startOffset, numResult, queryExpansion, false);
        int numberOfMissed = goldStd.getNumberMissed();

        //compute MRR for all queries
        mrrScore = eval.computeMRRScore(goldStd.getGoldRRScores());

        logger.info("Number of queries -> {}\t", numberOfQueries);
        logger.info("Number of not found -> {}\t", numberOfMissed);
        logger.info("Mean reciprocal rank (MRR) -> {} ", mrrScore);

        EvalResponse res = new EvalResponse(numberOfQueries, numberOfMissed, goldStd.getGoldRRMapping(), mrrScore);

        return new ResponseEntity<EvalResponse>(res, HttpStatus.OK);
    }

}

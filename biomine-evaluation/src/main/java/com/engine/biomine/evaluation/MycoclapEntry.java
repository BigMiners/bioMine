package com.engine.biomine.evaluation;
import com.engine.biomine.common.doc.LiteratureDoc;
import com.engine.biomine.docparsing.literature.PMCExtractor;
import com.engine.biomine.docparsing.literature.PubMedExtractor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by halmeida on 12/8/15.
 */
public class MycoclapEntry {

    private static ArrayList<LiteratureDoc> mycoclapList = new ArrayList<LiteratureDoc>();
    //enzyme ID, article
    HashMap<String,LiteratureDoc> PMCmapping = new HashMap<String,LiteratureDoc>();
    List<LiteratureDoc> mycoclapOpenAccess = new ArrayList<LiteratureDoc>();
    static final boolean verbose = true;

    public static void main(String[] args) {

        MycoclapEntry mycoclap = new MycoclapEntry();

        Path path = FileSystems.getDefault().getPath("/home/halmeida/Projects/MycoMINE/triage/corpus_mining_as_of_nov4/triage_corpus/positives");
        final PubMedExtractor extractor = new PubMedExtractor();

        String pathDB = "/home/halmeida/Projects/BioMine/bioMine/mycoCLAP_data.txt";
        mycoclap.list(path);
        List<LiteratureDoc> mycoclapOpenAccess = mycoclap.getEntrez();
        HashMap<String,LiteratureDoc> mycoclapOAMapping = mycoclap.mapMycoclapPMC(pathDB, mycoclapOpenAccess);

        System.out.println("mycoclapList size:" + mycoclapList.size());
        System.out.println("PMC open:" + mycoclapOpenAccess.size());
        System.out.println("mycoclap OA Mapping: " + mycoclapOAMapping.size());

    }


    private void list(Path path){

        try {

            Files.walkFileTree(path, new FileVisitor<Path>() {
                int totalCount = 0;
                LiteratureDoc temp = null;
                PubMedExtractor extractor = new PubMedExtractor();
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            boolean extensionIsValid = getFileExtension(file.toFile());
                            InputStream input = new FileInputStream(file.toFile());
                            if (extensionIsValid) {
                                totalCount++;
                                LiteratureDoc temp = (LiteratureDoc) extractor.extractDocValues(input);
                                if (temp.getPmc() != null) {
                                    //List all mycoclap entries that have a PMC
                                    if(verbose) System.out.println("mycoclap PMID with PMC: " + temp.getPmid());
                                    mycoclapList.add(temp);
                                }
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                            System.out.println("Could not read file '{}'" + file);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }
                    });


        } catch (IOException ex) {
            System.out.println("Could not iterate through resource directory '{}'" + path);
        }

    }


    /**
     * Retrieve all mycoclap PMCs that are open access
     * @return
     */
    private List<LiteratureDoc> getEntrez(){
        List<String> PMCIds = new ArrayList<String>();
        List<LiteratureDoc> mycoclapOpenAccess = new ArrayList<LiteratureDoc>();

        for(int i = 0; i < mycoclapList.size(); i++){
//            if(i < 10)
            PMCIds.add(mycoclapList.get(i).getPmc());
        }

        EntrezResponse response = new EntrezResponse();
        List<Document> results = response.getPmcDocResults(PMCIds);
        String exprXpathOpen = "//article[contains(@article-type,'research-article')]";
        XPath xpathExecutor = XPathFactory.newInstance().newXPath();

        PMCExtractor extractor = new PMCExtractor();

        for(Document doc : results) {

            Node currentNode;
            StringBuilder out = new StringBuilder();
            try {
                currentNode = (Node) xpathExecutor.evaluate(exprXpathOpen, doc, XPathConstants.NODE);
                if (currentNode != null && currentNode.hasChildNodes()) {
                        Node child = currentNode.getFirstChild().getNextSibling();
                        String value = child.getNodeValue();
                        if(value.contains("open_access")){

                            //creating an articleDocument for each open access
                            //article in mycoclap
                            LiteratureDoc temp = extractor.extractDocValues(doc);
                            String openAccessPMID = temp.getPmid();
                            String openAccessPMC = temp.getPmc();
                            mycoclapOpenAccess.add(temp);

                            if(verbose)System.out.println("openAccessPMID: " + openAccessPMID + "\t openAccessPMC: " + openAccessPMC);
                    }
                }
            } catch (XPathExpressionException ex) {
                System.out.println("XPathExpressionException: could not apply xpath expression" );
            }

        }

        return mycoclapOpenAccess;
    }

    /**
     *
     */
    private HashMap<String,LiteratureDoc> mapMycoclapPMC(String path, List<LiteratureDoc> mycoclapOpenAccess){
        String content = "";
        HashMap<String,LiteratureDoc> mycoclapOAMapping = new HashMap<String,LiteratureDoc>();

        try {
            //in Java 8....
            content = new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            System.out.println("Could not process file" + path);
        }

        String[] lines = content.split("\\n");

        for(int i=0; i < lines.length; i++){

            String[] oneLine = lines[i].split("\\t");

            if(oneLine.length > 2 && (oneLine[2] != null && !oneLine[2].isEmpty())){
                String enzymeId = oneLine[0];
                String mycoclapPMID = oneLine[2];

                for(LiteratureDoc doc : mycoclapOpenAccess){
                    String openAccessPMID = doc.getPmid();
                    String openAccessPMC = doc.getPmc();
                    if(openAccessPMID.contains(mycoclapPMID)){
                       if(verbose) System.out.println("enzyme id: " + enzymeId + "\t openAccessPMID: " + openAccessPMID + "\t openAccessPMC: " + openAccessPMC);
                        mycoclapOAMapping.put(enzymeId, doc);
                    }
                }
            }

        }

        return mycoclapOAMapping;
    }


    /**
     * Confirm file in folder has .XML extension
     * @param file
     * @return
     */
    private static boolean getFileExtension(File file) {
        String name = file.getName();
        try {
            if (name.endsWith(".xml") == true) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            return false;
        }
    }




}

package com.engine.biomine.common;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.util.StrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

/**
 * Created by halmeida on 2/24/16.
 */
public class IOUtil {


    private static IOUtil INSTANCE = new IOUtil();

    public static IOUtil getINSTANCE(){
        return INSTANCE;
    }
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public IOUtil(){
//        TConfig.get().setArchiveDetector(
//                new TArchiveDetector(
//                        TArchiveDetector.NULL,
//                        new Object[][]{
//                            {"tar", new TarDriver(IOPoolLocator.SINGLETON)},
//                            {"tgz|tar.gz", new TarGZipDriver(IOPoolLocator.SINGLETON)},
//                            {"tbz|tb2|tar.bz2", new TarBZip2Driver(IOPoolLocator.SINGLETON)},}));
    }


    public List<String> loadFileWithSeparator(String file, boolean splitLine, char separator){
        return loadFileWithSeparator(new File(file), splitLine, separator);
    }


    //for lists (stopwords, querywords)
    //for gff
    //for tsv
    public List<String> loadFileWithSeparator(File file, boolean splitLine, char separator){
        List<String> list = new ArrayList<>();
//
        InputStream stream = null;
        try{
            if(file.exists()) {//
                stream = getFileStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line = null;

                while ((line = reader.readLine()) != null) {
                    if (!line.contains("#")) {
                        if (splitLine)
                            list.addAll(StrUtils.splitSmart(line, separator));
                        else list.add(line);

                        line = reader.readLine();
                    }
                }
                stream.close();
                reader.close();
            }

        } catch (IOException e) {
            logger.error("Check that {} file exists in the your classpath or that you have set the parameter in the properties", file.getAbsolutePath());
            System.exit(0);
        }

        return list;
    }

    // for fasta
    public List<String> loadFileWithMultiLine(File file, char separator){
         List<String> list = new ArrayList<>();
         InputStream stream = null;

        try{
            if(file.exists()) {

//                stream = getFileStream(file, isFromArchive);
                stream = getFileStream(file);

                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line = null;
                StringBuilder sb = new StringBuilder();

                while ((line = reader.readLine()) != null) {

                    if (!line.contains("#")) {

                        String thisline = line;

                        if (thisline.contains(String.valueOf(separator))) {
                            thisline += "\t";

                            if (sb.length() > 1)
                                list.add(sb.toString());

                            sb.setLength(0);
                            sb.append(thisline);
                        } else sb.append(thisline);
                    }

                }
                stream.close();
                reader.close();
            }
        } catch (IOException e) {
            logger.error("Check that {} file exists in the your classpath or that you have set the parameter in the properties", file.getAbsolutePath());
            System.exit(0);
        }
        return list;
    }

     /**
     * Loads list found in a file.
     * (e.g. queries, qrel...)
     * HA
     * @param file
     * @return
     */
    public HashMap<String,String> loadTabMapFile(String file){

        HashMap<String,String> list = new HashMap<>();

        try{
            String featureLine = "";

            //listing features
            BufferedReader reader = new BufferedReader(new FileReader(file));

            int featureCount = 0;
            while (( featureLine = reader.readLine()) != null) {

                String[] content = StringUtils.split(featureLine,"\n");

                for(int i = 0; i < content.length; i++) {

                    if (!content[i].contains("#")) {

                        String[] oneLine = StringUtils.split(content[i], "\t");

                        String id = oneLine[0];
                        String value = oneLine[1];
                        if (!list.keySet().contains(id)) {
                            list.put(id, value);
                        } else {
                            //if entry already exists, just add new values to it
                            String temp = list.get(id) + "," + value;
                            list.put(id, temp);
                        }
                    }
                }
            }
            reader.close();
        }
        catch (IOException e) {
            logger.error("Check that {} file exists in the your classpath or that you have set the parameter in the properties", file);
            System.exit(0);
        }
        return list;
    }


	 /**
     * Generic function for writing something to a given file
     */
    public void writeOutput(String path, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write(content);
        writer.flush();
        writer.close();
        System.out.println("File outputted in " + path);
    }

    //change this to read from config file
    public boolean isValidExtension(String path){
        try {
            String ext = path.substring(path.lastIndexOf("."));
            if (ext.endsWith("xml")
                    || ext.endsWith("nxml")
                    || ext.endsWith("tar")
                    || ext.endsWith("zip")
                    || ext.endsWith("tar.gz")
                    || ext.endsWith("gz")
                    || ext.endsWith("gff3")
                    || ext.endsWith("tsv")
                    || ext.endsWith("fna")
                    || ext.endsWith("faa")
                    || ext.endsWith("fa")
                    )
                return true;
            else {
                logger.debug("Not valid file extension. {} ", path);
                return false;
            }
        }catch(StringIndexOutOfBoundsException e){
            logger.info("Error with file extension. {} ", path);
        }
        return false;
    }

	public String getFileExtension(File file) {
        String name = file.getName();
        try {
            if (name.endsWith(".tar.gz") == true) {
                return ".tar.gz";
            } else {
                return name.substring(name.lastIndexOf("."));
            }
        } catch (Exception e) { return ""; }
    }


    /**
     * Returns a (commonscompress) list of files for a
     * tar.gz compressed file
     *
     * @param file
     * @return
     */
    public InputStream getFileStream(File file){


        String ext = getFileExtension(file);
        BufferedInputStream bs = null;

        try{
            switch(ext) {
                case (".zip"):
                    return new ZipInputStream(new FileInputStream(file));
                case(".tgz"):
                case(".tar.gz"):
                case(".tar"):
                    bs = new BufferedInputStream(new GZIPInputStream(new FileInputStream(file)));
                    return new TarArchiveInputStream(bs);
                case(".gz"):
                    return new GZIPInputStream(new FileInputStream(file));
                default:
                    return new FileInputStream(file);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File was not found: " + file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bs;
    }


    public File retrieveCompressedFile(InputStream ais, String entry){

        File currentfile = null;

            int count;
            byte data[] = new byte[2048];

            int position = entry.contains("/") == true ? entry.lastIndexOf("/")+1 : 0;
            String fName = entry.substring(position);

            if (IOUtil.getINSTANCE().isValidExtension(fName)) {
                currentfile = new File("/tmp/" + System.currentTimeMillis() + "." + fName);
                try {
                    //untar the currentfile
                    BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(currentfile));

                    while ((count = ais.read(data)) != -1) {
                        outputStream.write(data, 0, count);
                    }
                    outputStream.flush();
                    outputStream.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        return currentfile;
    }


//   /**
//     * Returns a (trueZip) list of files for a
//     * tar.gz compressed file
//     *
//     * @param file
//     * @return
//     */
//    public TFile[] decompressTarGz(File file) {
//        TFile[] directories = new TFile(file, TConfig.get().getArchiveDetector()).listFiles();
//        return directories;
//    }


    /**
     * Exports feature list to a given file
     * HA
     * @param location
     * @param list
     */
    public void exportFeatures(String location, HashMap<String,Integer> list, int numberDocs, String step){
        String SEPARATOR = "\n";
        StringBuffer line = new StringBuffer();

        if(!step.contains("UniTest")) {
            try {

                for (Map.Entry<String, Integer> entry : list.entrySet()) {
                    if (entry != null) {
                        String str = entry.getKey() + "\t" + entry.getValue();
                        line.append(str).append(SEPARATOR);
                    }
                }

                BufferedWriter writer = new BufferedWriter(new FileWriter(location, false));

                writer.write((line.toString()));
                writer.flush();
                writer.close();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}

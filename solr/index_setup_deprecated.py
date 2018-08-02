#!/usr/bin/python
import os,sys,argparse,shutil
import logging as logger
import ConfigParser
import subprocess
import distutils
import time

logger.basicConfig(level=logger.INFO, format='%(asctime)s -- %(message)s')

class SolrSetup:
    def __init__(self, configFile):
        self.config = configFile

    def runSetup(self,setupZK=None):     
        if setupZK == None:
            setupZK = False
        
        self.installZK(setupZK)
        self.installSolr()

    def installSolr(self):
        #installing solr
        logger.info("Start installing Solr")
        solrBinFolder = self.getProperty('solr.home.path')
        if (self.folderExists(solrBinFolder) == False):
            logger.error(('Solr binaries folder (%s) folder does not exists. You must select a valid folder')%(solrBinFolder))
            sys.exit()

        solrConfigFolder = self.getProperty('solr.index.config.path')
        if (self.folderExists(solrConfigFolder) == False):
            logger.error(('Solr configuration folder (%s) folder does not exists. You must select a valid folder')%(solrConfigFolder))
            sys.exit()
        
        solrDataFolder = self.getProperty('solr.data.path')
        if (self.folderExists(solrDataFolder) == True):
            logger.error(('Solr data folder (%s) already exists. You must select a valid folder')%(solrDataFolder))
            sys.exit()

        # #create data folder
        os.makedirs(solrDataFolder)
        os.makedirs(solrDataFolder + '/logs')

        zkDataFolder = self.getProperty('zk.data.path')
        #create solr.conf file
        solrConfigWriter = open(solrDataFolder + '/solrConfig.cfg', "w")
        solrConfigWriter.write("# solr port\n")        
        solrConfigWriter.write(("SOLR_PORT=%s\n")%(str(self.getProperty("solr.port"))))
        solrConfigWriter.write("# solr host\n")
        solrConfigWriter.write(("SOLR_HOST=%s\n")%(str(self.getProperty("solr.host"))))
        solrConfigWriter.write("# solr distribution directory\n")
        solrConfigWriter.write(("SOLR_BIN_DIR=%s\n")%(str(self.getProperty("solr.home.path"))))
        solrConfigWriter.write("# solr data dir\n")
        solrConfigWriter.write(("SOLR_DATA_DIR=%s\n")%(str(self.getProperty("solr.data.path"))))
        solrConfigWriter.write("# zookeeper node 10.10.4.250:2181/solr\n")
        solrConfigWriter.write(("ZK_NODE=%s:%s/solr\n")%(self.getProperty("solr.host"),self.getProperty("zk.port")))

        solrConfigWriter.write("# solr log folder\n")
        solrConfigWriter.write(("SOLR_LOG_DIR=%s/logs\n")%(self.getProperty("solr.data.path")))

        solrConfigWriter.write("# heap space for Solr JVM\n")        
        solrConfigWriter.write(("JAVA_HEAP_SIZE=%s\n")%(str(self.getProperty("solr.memory.heap"))))
        solrConfigWriter.close()

        shutil.copyfile('./biomine_solr.sh', solrDataFolder + '/biomine_solr.sh')

        #copy solr.xml to data dir
        shutil.copy("./solr.xml", solrDataFolder + "/solr.xml")

        #start solr
        logger.info("Starting solr and start creating shards and collections")
        solrConfigFile = solrDataFolder + '/solrConfig.cfg'
        solrStartup = solrDataFolder + '/biomine_solr.sh'
        os.chmod(solrStartup, 0o777)
        solrProcess = subprocess.Popen(['nohup', solrStartup, "start"], stdin=subprocess.PIPE, stdout=subprocess.PIPE, close_fds=True)

        #force sleep to wait for solr
        time.sleep(5)
        #create biomine collection with shards
        solrUrl = ("http://%s:%s/solr")%(self.getProperty("solr.host"),self.getProperty("solr.port"))
        numShards = str(self.getProperty("solr.index.nbShards"))
        collectionName = "literature"
        configName = "literature" #name of the configuration in zookeeper, here the configName is similar to the collection name
        curlCmd = ("%s/admin/collections?action=CREATE&name=%s&numShards=%s&maxShardsPerNode=64&replicationFactor=1&collection.configName=%s")%(solrUrl,collectionName,numShards,configName)        
        collectionProcess = subprocess.Popen(['nohup', 'curl' ,"-f", curlCmd], stdin=subprocess.PIPE, stdout=subprocess.PIPE, close_fds=True)
        time.sleep(5)
        logger.info("The index should be up and running please visit: " + solrUrl)
        
    def installZK(self,ignoreFlag):
        #do not install ZK
        if ignoreFlag == True:
            return
        
        solrBinFolder = self.getProperty('solr.home.path')
        #install ZK
        logger.info("Start installing Zookeeper")
        zkBinFolder = self.getProperty('zk.home.path')
        if (self.folderExists(zkBinFolder) == False):
            logger.error(('Zookeeper %s does not exists')%(zkBinFolder))
            sys.exit()

        #copy default zk config to the target folder
        zkDataFolder = self.getProperty('zk.data.path')
        if(self.folderExists(zkDataFolder) == True):
            logger.error(('Zookeeper %s already exists. You must select a different folder')%(zkDataFolder))
            sys.exit()
        else:
            #create the folder
            os.makedirs(zkDataFolder  + "/bin/biomine_zkcli/")

            #update zk config file
            fReader = open(zkBinFolder + '/conf/zoo_sample.cfg', "r")
            cnt = fReader.read()            
            fReader.close()
            cnt = cnt.replace("dataDir=/tmp/zookeeper","dataDir=" + zkDataFolder)
            zkPort = self.getProperty("zk.port")
            cnt = cnt.replace("clientPort=2181","clientPort=" + zkPort)
            #write new config
            fWriter = open(zkDataFolder + '/zkConfig.cfg', "w")
            fWriter.write(str(cnt))
            fWriter.close()

        #copy log4j file
        shutil.copyfile(solrBinFolder + '/server/scripts/cloud-scripts/log4j.properties', zkDataFolder + '/bin/biomine_zkcli/log4j.properties')
        #copy solr_war lib
        os.makedirs(zkDataFolder + '/bin/biomine_zkcli/solr.war_lib')
        for sFile in os.listdir(solrBinFolder + '/server/solr-webapp/webapp/WEB-INF/lib'):
            shutil.copyfile(solrBinFolder + '/server/solr-webapp/webapp/WEB-INF/lib/' + sFile, zkDataFolder + '/bin/biomine_zkcli/solr.war_lib/' + sFile)

        os.makedirs(zkDataFolder + '/bin/biomine_zkcli/lib_ext')
        for sFile in os.listdir(solrBinFolder + '/server/lib/ext'):
            shutil.copyfile(solrBinFolder + '/server/lib/ext/' + sFile, zkDataFolder + '/bin/biomine_zkcli/lib_ext/' + sFile)

        zkStartScript = []
        zkStartScript.append("#!/bin/bash")
        zkStartScript.append("SOLR_ZK_CLI_DIR=\"`dirname \"$0\"`\"\n")
        zkStartScript.append("java -Dlog4j.configuration=file:$SOLR_ZK_CLI_DIR/biomine_zkcli/log4j.properties -classpath \"$SOLR_ZK_CLI_DIR/biomine_zkcli/solr.war_lib/*:$SOLR_ZK_CLI_DIR/biomine_zkcli/lib_ext/*\" org.apache.solr.cloud.ZkCLI ${1+\"$@\"}")

        zkCliFile = zkDataFolder + '/bin/biomine_zkCli.sh'
        fWriter = open(zkCliFile,'w')
        fWriter.write("\n".join(zkStartScript))
        fWriter.close()
        os.chmod(zkCliFile, 0o777)

        #create tlog for zookeeper
        os.makedirs(zkDataFolder + '/tlog')
        
        #start zookeeper and load solr config
        logger.info("Starting Zookeeper and loading configuration")
        cmd = zkBinFolder + '/bin/zkServer.sh'
        zkConfigFile = zkDataFolder + '/zkConfig.cfg'
        zkProcess = subprocess.Popen(['nohup',cmd, "start", zkConfigFile], stdin=subprocess.PIPE, stdout=subprocess.PIPE, close_fds=True)
        logger.info("Started Zookeeper")
        zkPort = str(self.getProperty("zk.port"))
        indexConfig = self.getProperty("solr.index.config.path") + "/conf/"
        logger.info("Uploading literature config")
        cmd = zkDataFolder + '/bin/biomine_zkCli.sh'
        uploadProcess = subprocess.Popen(["nohup",cmd, "-zkhost", "localhost:"+zkPort+"/solr", "-cmd","upconfig","-confdir",indexConfig,"-confname","literature"], stdin=subprocess.PIPE, stdout=subprocess.PIPE, close_fds=True)

        solrXmlFile = self.getProperty("solr.solrXml")
        #upload solr.xml
        uploadSolrXmlProcess = subprocess.Popen(["nohup",cmd, "-zkhost", "localhost:"+zkPort+"/solr", "-cmd","putfile","/solr.xml",solrXmlFile], stdin=subprocess.PIPE, stdout=subprocess.PIPE, close_fds=True)
        logger.info("Done loading config")

    def getProperty(self, propertyName):
        return self.config.get('setupConfigSection', propertyName)

    def folderExists(self, folderPath):
        return os.path.exists(folderPath)


def main(args):
    #parse command arguments
    parser = argparse.ArgumentParser(description='bioMine index setup utility')
    parser.add_argument("--config","-c", required=True, help='path to install.config file')
    parser.add_argument("--zkFlag","-z", required=False, action='store_true', default=False, help='install zookeeper')
    args = parser.parse_args()
    #config file
    setupConfigFile = args.config
    #parse the setup config file
    setupConfigs = ConfigParser.RawConfigParser()
    setupConfigs.readfp(open(setupConfigFile))

    #flag to ignore setup of ZK
    ignoreZK = args.zkFlag

    x = SolrSetup(setupConfigs)
    x.runSetup(setupZK=ignoreZK)

if __name__ == '__main__':
    main(sys.argv)

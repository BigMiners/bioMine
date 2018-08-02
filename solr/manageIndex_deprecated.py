#!/usr/bin/python
import os, sys,argparse
import logging as logger
import urllib2

logger.basicConfig(level=logger.INFO, format='%(asctime)s %(message)s')

class Manager:    
    def __init__(self, serverUrl, collectionName):
        self.server = "http://" + serverUrl
        self.collection = collectionName

    def processCmd(self, action, shardId=None):
        cmd_text = None
        if action.lower() == "add_shard":
            logger.info("Adding shard to " + self.server)
            #add shard command
            cmd_text = ("%s/solr/admin/collections?action=CREATESHARD&shard=%s&collection=%s")%(self.server,shardId,self.collection)            

        if action.lower() == "delete_docs":
            logger.info("Remove all documents from index")
            cmd_text = ("%s/solr/%s/update?stream.body=<delete><query>*:*</query></delete>")%(self.server,self.collection)            

        if action.lower() == "delete_shard":
            logger.info("Deleting shard")
            cmd_text = ("%s/solr/admin/collections?action=DELETESHARD&shard=%s&collection=%s")%(self.server,shardId,self.collection)

        if action.lower() == "status":
            logger.info("Status of all nodes")
            cmd_text = ("%s/solr/admin/cores?action=STATUS")%(self.server)

        if cmd_text == None:
            logger.warn("No command executed")
            return False
            
        logger.debug(cmd_text)
        #send command to the server        
        self.execCmd(cmd_text)

        #commit changes to the index after executing the action
        self.commit()
    
    def execCmd(self, cmdText):
        #force solr to return json formatted results
        cmdText = cmdText + "&wt=json&indent=true"
        res = urllib2.urlopen(cmdText).read()
        sys.stdout.write(res)
        

    def commit(self):
        commit_text = ("%s/solr/%s/update?stream.body=<commit/>")%(self.server, self.collection)
        self.execCmd(commit_text)

def main(args):    
    #parse command arguments
    parser = argparse.ArgumentParser(description='Solr Server utility')
    parser.add_argument("--server","-s", required=True, help='url of solr server 127.0.0.1:8983')
    parser.add_argument("--collection", required=False, default="core1", help='name of the solr collection. Default="core1"')
    parser.add_argument("-c","--cmd", required=True, choices=['ADD_SHARD','DELETE_SHARD','DELETE_DOCS','STATUS'], help='action to execute on solr')
    parser.add_argument('--shardId', help="target shard")
    args = parser.parse_args()
    #address of solr server
    solrServer = args.server
    #type of action
    actionCmd = args.cmd
    #name of the solr collection
    collection = args.collection    

    #create cluster object
    cluster = Manager(solrServer, collection)
    
    cluster.processCmd(actionCmd, args.shardId)

if __name__ == '__main__':
    main(sys.argv)

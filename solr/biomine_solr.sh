#!/bin/bash

usage="biomine_solr.sh (start|stop)"

if [[ $# != 1 ]]; then
    echo $usage
    exit
fi

source $(readlink -f $0 | xargs dirname)/solrConfig.cfg

SOLR_STOP_PORT=`expr $SOLR_PORT + 1` || true
SOLR_STOP_KEY=biomine

echo $SOLR_HOST
SOLR_JMX_PORT=`expr $SOLR_PORT + 2` || true

# Set Solr data dir internals
SOLR_LOG_DIR=$SOLR_DATA_DIR/logs
SOLR_PID_FILE=$SOLR_DATA_DIR/solr_$SOLR_PORT.pid

# Solr console logs (turned of by default in log4j.properties)
SOLR_OUT=/dev/null
SOLR_ERR=/dev/null

# Allow changes in heap size
JAVA_ARGS="-server -Xss256k -Xms256m -Xmx$JAVA_HEAP_SIZE"

# Misc. Java args - by default in solr start script
JAVA_ARGS="$JAVA_ARGS -XX:NewRatio=3 -XX:SurvivorRatio=4 -XX:TargetSurvivorRatio=90 -XX:MaxTenuringThreshold=8 -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:ConcGCThreads=4 -XX:ParallelGCThreads=4 -XX:+CMSScavengeBeforeRemark -XX:PretenureSizeThreshold=64m -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=50 -XX:CMSMaxAbortablePrecleanTime=6000 -XX:+CMSParallelRemarkEnabled -XX:+ParallelRefProcEnabled -XX:CMSFullGCsBeforeCompaction=1 -XX:CMSTriggerPermRatio=80 -verbose:gc -XX:+PrintHeapAtGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+PrintTenuringDistribution -XX:+PrintGCApplicationStoppedTime"

# Foce timezone - by default in solr start script
JAVA_ARGS="$JAVA_ARGS -Duser.timezone=UTC"

# Prefere IPv4 - by default in solr start script
JAVA_ARGS="$JAVA_ARGS -Djava.net.preferIPv4Stack=true"


SOLR_ARGS="-DzkClientTimeout=15000 -DzkHost=${ZK_NODE} -DSTOP.PORT=$SOLR_STOP_PORT -DSTOP.KEY=$SOLR_STOP_KEY -Dhost=${SOLR_HOST} -Djetty.port=${SOLR_PORT} -Djetty.home=${SOLR_BIN_DIR}/server -Djetty.base=${SOLR_BIN_DIR}/server -Dsolr.solrxml.location=zookeeper -Dsolr.install.dir=${SOLR_BIN_DIR} -Dsolr.solr.home=${SOLR_DATA_DIR} -Dsolr.log=${SOLR_LOG_DIR} -Xloggc:$SOLR_LOG_DIR/solr_gc.log"

SOLR_URL_SCHEME=http
SOLR_JETTY_CONFIG=()
SOLR_SSL_OPTS=""
if [ -n "$SOLR_SSL_KEY_STORE" ]; then
  SOLR_JETTY_CONFIG+=("--module=https")
  SOLR_URL_SCHEME=https
  SOLR_SSL_OPTS=" -Dsolr.ssl.checkPeerName=false \
    -Dsolr.jetty.keystore=$SOLR_SSL_KEY_STORE \
    -Dsolr.jetty.keystore.password=$SOLR_SSL_KEY_STORE_PASSWORD \
    -Dsolr.jetty.truststore=$SOLR_SSL_TRUST_STORE \
    -Dsolr.jetty.truststore.password=$SOLR_SSL_TRUST_STORE_PASSWORD \
    -Dsolr.jetty.ssl.needClientAuth=$SOLR_SSL_NEED_CLIENT_AUTH \
    -Dsolr.jetty.ssl.wantClientAuth=$SOLR_SSL_WANT_CLIENT_AUTH"
  if [ -n "$SOLR_SSL_CLIENT_KEY_STORE" ]; then
    SOLR_SSL_OPTS+=" -Djavax.net.ssl.keyStore=$SOLR_SSL_CLIENT_KEY_STORE \
      -Djavax.net.ssl.keyStorePassword=$SOLR_SSL_CLIENT_KEY_STORE_PASSWORD \
      -Djavax.net.ssl.trustStore=$SOLR_SSL_CLIENT_TRUST_STORE \
      -Djavax.net.ssl.trustStorePassword=$SOLR_SSL_CLIENT_TRUST_STORE_PASSWORD"
  else
    SOLR_SSL_OPTS+=" -Djavax.net.ssl.keyStore=$SOLR_SSL_KEY_STORE \
      -Djavax.net.ssl.keyStorePassword=$SOLR_SSL_KEY_STORE_PASSWORD \
      -Djavax.net.ssl.trustStore=$SOLR_SSL_TRUST_STORE \
      -Djavax.net.ssl.trustStorePassword=$SOLR_SSL_TRUST_STORE_PASSWORD"
  fi

  SOLR_ARGS+=$SOLR_SSL_OPTS

else
  SOLR_JETTY_CONFIG+=("--module=http")
fi


${SOLR_BIN_DIR}/server/scripts/cloud-scripts/zkcli.sh -zkhost ${ZK_NODE} -cmd clusterprop -name urlScheme -val $SOLR_URL_SCHEME
zk_cmd_ret=$?
if [ $zk_cmd_ret -ne 0 ]; then
     echo "Failed to set urlScheme to $SOLR_URL_SCHEME"
fi



Start() {
    echo "*** Starting Solr from $SOLR_LOG_DIR ..."
    # Running from SOLR_LOG_DIR so that velocity.log is in the right place
    (cd "$SOLR_LOG_DIR"; \
        nohup java $JAVA_ARGS $SOLR_ARGS -jar "$SOLR_BIN_DIR/server/start.jar" $SOLR_JETTY_CONFIG)
}

Stop() {
    echo "*** Stopping Solr gracefully..."
    (cd "$SOLR_LOG_DIR"; \
        java $SOLR_ARGS -jar "$SOLR_BIN_DIR/server/start.jar" $SOLR_JETTY_CONFIG --stop)
    sleep 2
}

while [ $# -ne 0 ]; do
    case $1 in
        "start")
            shift
            Start
            ;;
        "stop")
            shift
            Stop
            ;;
        *)
            usage
            ;;
    esac
done

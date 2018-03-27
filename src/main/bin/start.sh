#!/bin/sh

BASEDIR="/opt/app/data-router/"
AJSC_HOME="$BASEDIR"
AJSC_CONF_HOME="$AJSC_HOME/bundleconfig/"

if [ -z "$CONFIG_HOME" ]; then
        echo "CONFIG_HOME must be set in order to start up process"
        exit 1
fi


PROPS="-DAJSC_HOME=$AJSC_HOME"
PROPS="$PROPS -DAJSC_CONF_HOME=$AJSC_CONF_HOME"
PROPS="$PROPS -Dlogging.config=$BASEDIR/bundleconfig/etc/logback.xml"
PROPS="$PROPS -DAJSC_SHARED_CONFIG=$AJSC_CONF_HOME"
PROPS="$PROPS -DAJSC_SERVICE_NAMESPACE=data-router"
PROPS="$PROPS -DAJSC_SERVICE_VERSION=v1"
PROPS="$PROPS -Dserver.port=9502"
PROPS="$PROPS -DCONFIG_HOME=$CONFIG_HOME"
JVM_MAX_HEAP=${MAX_HEAP:-1024}

echo $CLASSPATH

cd ${MICRO_HOME}
jar uf0 $MICRO_HOME/data-router.jar BOOT-INF/lib/*

exec java -Xmx${JVM_MAX_HEAP}m $PROPS -jar ${MICRO_HOME}/data-router.jar
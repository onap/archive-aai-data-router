#!/bin/sh

BASEDIR="/opt/app/data-router/"
AJSC_HOME="$BASEDIR"


if [ -z "$CONFIG_HOME" ]; then
        echo "CONFIG_HOME must be set in order to start up process"
        exit 1
fi

if [ -z "$KEY_STORE_PASSWORD" ]; then
	echo "KEY_STORE_PASSWORD must be set in order to start up process"
	exit 1
fi

PROPS="-DAJSC_HOME=$AJSC_HOME"
PROPS="$PROPS -Dlogging.config=$BASEDIR/bundleconfig/etc/logback.xml"
PROPS="$PROPS -DCONFIG_HOME=$CONFIG_HOME"
PROPS="$PROPS -DKEY_STORE_PASSWORD=$KEY_STORE_PASSWORD"

if [ ! -z "$TRUST_STORE_PASSWORD" ]; then
   PROPS="$PROPS -DTRUST_STORE_PASSWORD=${TRUST_STORE_PASSWORD}"
fi

if [ ! -z "$TRUST_STORE_LOCATION" ]; then
   PROPS="$PROPS -DTRUST_STORE_LOCATION=${TRUST_STORE_LOCATION}"
fi


JVM_MAX_HEAP=${MAX_HEAP:-1024}

cd ${MICRO_HOME}
jar uf0 $MICRO_HOME/data-router.jar BOOT-INF/lib/*

exec java -Xmx${JVM_MAX_HEAP}m $PROPS -jar ${MICRO_HOME}/data-router.jar
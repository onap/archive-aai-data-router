#!/bin/sh

BASEDIR="/opt/app/data-router/"
AJSC_HOME="$BASEDIR"
AJSC_CONF_HOME="$AJSC_HOME/bundleconfig/"

if [ -z "$CONFIG_HOME" ]; then
        echo "CONFIG_HOME must be set in order to start up process"
        exit 1
fi

if [ -z "$KEY_STORE_PASSWORD" ]; then
        echo "KEY_STORE_PASSWORD must be set in order to start up process"
        exit 1
else
        echo "KEY_STORE_PASSWORD=$KEY_STORE_PASSWORD\n" >> $AJSC_CONF_HOME/etc/sysprops/sys-props.properties
fi

if [ -z "$KEY_MANAGER_PASSWORD" ]; then
        echo "KEY_MANAGER_PASSWORD must be set in order to start up process"
        exit 1
else
        echo "KEY_MANAGER_PASSWORD=$KEY_MANAGER_PASSWORD\n" >> $AJSC_CONF_HOME/etc/sysprops/sys-props.properties
fi

# Add any routes configured at deploy time to the data layer service
if [ -n "$DYNAMIC_ROUTES" ]; then
        echo "Adding the following dynamic routes to the deployment: "
        mkdir -p /tmp/data-router/v1/routes
        for f in `ls $DYNAMIC_ROUTES`
        do
                cp $DYNAMIC_ROUTES/$f /tmp/data-router/v1/routes
                echo "Adding dynamic route $DYNAMIC_ROUTES/$f"
        done
        jar uf /opt/app/data-router/services/data-router_v1.zip* -C /tmp/ data-router
        rm -rf /tmp/data-router
fi

# Add any spring bean configuration files to the data layer deployment
if [ -n "$SERVICE_BEANS" ]; then
        echo "Adding the following dynamic service beans to the deployment: "
        mkdir -p /tmp/data-router/v1/conf
        for f in `ls $SERVICE_BEANS`
        do
                cp $SERVICE_BEANS/$f /tmp/data-router/v1/conf
                echo "Adding dynamic service bean $SERVICE_BEANS/$f"
        done
        jar uf /opt/app/data-router/services/data-router_v1.zip* -C /tmp/ data-router
        rm -rf /tmp/data-router
fi

# Add any dynamic component configuration files to the data layer deployment
if [ -n "$COMPLIB" ]; then
        echo "Adding the following dynamic libraries to the deployment: "
        mkdir -p /tmp/data-router/v1/lib
        for f in `ls $COMPLIB`
        do
                cp $COMPLIB/$f /tmp/data-router/v1/lib
                echo "Adding dynamic library $COMPLIB/$f"
        done
        jar uf /opt/app/data-router/services/data-router_v1.zip* -C /tmp/ data-router
        rm -rf /tmp/data-router
fi

CLASSPATH="$AJSC_HOME/lib/*"
CLASSPATH="$CLASSPATH:$AJSC_HOME/extJars/"
CLASSPATH="$CLASSPATH:$AJSC_HOME/etc/"
PROPS="-DAJSC_HOME=$AJSC_HOME"
PROPS="$PROPS -DAJSC_CONF_HOME=$AJSC_CONF_HOME"
PROPS="$PROPS -Dlogback.configurationFile=$BASEDIR/bundleconfig/etc/logback.xml"
PROPS="$PROPS -DAJSC_SHARED_CONFIG=$AJSC_CONF_HOME"
PROPS="$PROPS -DAJSC_SERVICE_NAMESPACE=data-router"
PROPS="$PROPS -DAJSC_SERVICE_VERSION=v1"
PROPS="$PROPS -Dserver.port=9502"
PROPS="$PROPS -DCONFIG_HOME=$CONFIG_HOME"

echo $CLASSPATH

java -Xms1024m -Xmx4096m -XX:PermSize=2024m $PROPS -classpath $CLASSPATH com.att.ajsc.runner.Runner context=// sslport=9502

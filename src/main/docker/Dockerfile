FROM @aai.docker.namespace@/aai-common-@aai.base.image@:@aai.base.image.version@

ARG MICRO_HOME=/opt/app/data-router
ARG BIN_HOME=$MICRO_HOME/bin
ARG DR_LOGS=/var/log/onap/AAI-DR

# AAI-2176 Additional optional steps for Ubuntu
RUN if  ((uname -v | grep -i "ubuntu") || ( cat /etc/*release|grep -i "ubuntu")      ) ; then \
   #####################################
   #if JAVA doesnt exist then install it: 
   ######################################
   if type  java  2>/dev/null; then \ 	
     echo "skipping java installation"; \  	
   else \
     apt-get update && apt-get install -y software-properties-common; \
     ## sudo -E is required to preserve the environment. If you remove that line, it will most like freeze at this step
     sudo -E add-apt-repository ppa:openjdk-r/ppa && apt-get update && apt-get install -y openjdk-8-jdk ; \
     ## Setup JAVA_HOME, this is useful for docker commandline
     JAVA_HOME=usr/lib/jvm/java-8-openjdk-amd64; \
     export JAVA_HOME ; \
   fi; \
fi
RUN mkdir -p $MICRO_HOME $MICRO_HOME/logs $DR_LOGS

RUN groupadd aaiadmin -g 1000
RUN adduser -u 1000 -h /opt/aaihome/aaiadmin -S -D -G aaiadmin -s /bin/bash aaiadmin

WORKDIR $MICRO_HOME
RUN chown -R aaiadmin:aaiadmin $MICRO_HOME $MICRO_HOME/logs $DR_LOGS

COPY --chown=aaiadmin:aaiadmin /maven/data-router/ .

USER aaiadmin

RUN chmod 755 $BIN_HOME/* \
    && ln -snf /logs $MICRO_HOME/logs

EXPOSE 9502 9502

CMD ["/opt/app/data-router/bin/start.sh"]

FROM aaionap/aai-common:@aai.docker.version@-@aai.base.image@

ARG MICRO_HOME=/opt/app/data-router
ARG BIN_HOME=$MICRO_HOME/bin

RUN mkdir -p $MICRO_HOME
WORKDIR $MICRO_HOME
COPY /maven/data-router/ .
RUN chmod 755 $BIN_HOME/* \
    && ln -snf /logs $MICRO_HOME/logs

EXPOSE 9502 9502

CMD ["/opt/app/data-router/bin/start.sh"]

FROM ubuntu:14.04

ARG MICRO_HOME=/opt/app/data-router
ARG BIN_HOME=$MICRO_HOME/bin

RUN apt-get update

# Install and setup java8
RUN apt-get update && apt-get install -y software-properties-common
## sudo -E is required to preserve the environment. If you remove that line, it will most like freeze at this step
RUN sudo -E add-apt-repository ppa:openjdk-r/ppa && apt-get update && apt-get install -y openjdk-8-jdk
## Setup JAVA_HOME, this is useful for docker commandline
ENV JAVA_HOME usr/lib/jvm/java-8-openjdk-$(dpkg --print-architecture)
RUN export JAVA_HOME

# Build up the deployment folder structure
RUN mkdir -p $MICRO_HOME
RUN mkdir -p $BIN_HOME
RUN mkdir -p $MICRO_HOME/bundleconfig
RUN mkdir -p $MICRO_HOME/bundleconfig/etc
RUN mkdir -p $MICRO_HOME/BOOT-INF/lib
COPY start.sh $MICRO_HOME/bin/
RUN chmod 755 $BIN_HOME/*
RUN ln -s /logs $MICRO_HOME/logs
ENV MICRO_HOME ${MICRO_HOME}
ENV CONFIG_HOME=${CONFIG_HOME}
COPY data-router.jar $MICRO_HOME/
COPY bundleconfig-local $MICRO_HOME/bundleconfig
COPY bundleconfig-local/etc/logback.xml $MICRO_HOME/bundleconfig/etc


EXPOSE 9502 9502

CMD ["/opt/app/data-router/bin/start.sh"]

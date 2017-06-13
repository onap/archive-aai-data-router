# ONAP Functions

The _Synapse_ data router comes packaged with a set of functionality that needs to be configured (as described at [Synapse README](./README.md)). This section describes specific functionality that _Synapse_ is capable of fulfilling given the appropriate configuration.

## Entity Change Event


### Configuration

##### Contents of the /opt/app/datarouter-service/dynamic/conf Directory

_entity-event-policy.xml_

In order to take advantage of the entity change event functionality the following configuration spring bean file should be present with the appropriate fields filled in:

	<beans xmlns="http://www.springframework.org/schema/beans"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="
                        http://www.springframework.org/schema/beans
                        http://www.springframework.org/schema/beans/spring-beans.xsd">

        <bean id="eepConfig" class="org.openecomp.datarouter.policy.EntityEventPolicyConfig" >
        	<property name="sourceDomain" value="{domain-origin}" />
           <property name="searchBaseUrl" value="https://{search-service-host}:{search-service-port}" />
           <property name="searchEndpoint" value="services/search-data-service/v1/search/indexes/" />
           <property name="searchEndpointDocuments" value = "documents" />
           <property name="searchEntitySearchIndex" value="{entity-search-index-name}" />
           <property name="searchTopographySearchIndex" value="{topographical-search-index-name}" />
           <property name="searchEntityAutoSuggestIndex" value="{entity-auto-suggest-index-name}" />
           <property name="searchAggregationVnfIndex" value="{aggregation-vnf-index-name}" />
           <property name="searchCertName" value="{search-service-certificate-name}" />
           <property name="searchKeystorePwd" value="OBF:{obfuscated-keystore-password}" />
           <property name="searchKeystore" value="{keystore-name}" />
        </bean>

        <bean id="entityEventPolicy" class="org.openecomp.datarouter.policy.EntityEventPolicy" init-method="startup" >
            <constructor-arg ref="eepConfig"/>
        </bean>
	</beans>

Description of configuration fields:

sourceDomain: AAI adds a domain tag against all entity change event records published to the event bus. If the configured sourceDomain does not match with the domain value provided in the entity change event, that event is silently discarded.
searchBaseURL: the URL for the search service that this version of Synapse should send entity change records to. The format should be https://host:port. 
searchEndpoint: the base endpoint for the indexes resource on the search service. There should be no need to modify this value from the default unless the version of the search service API changes.
searchEndpointDocuments: the resource path identifier for documents in the search service. This value is appended to the searchEndpoint to build up the resource path for documents. There should be no need to modfiy this value from the default.
searchEntitySearchIndex: the name of the index in search service that will hold the entity change record.
searchTopographySearchIndex: the index name for storing topographical data in the search service. This index holds geographic coordinates for entities, and is used to plot points on a map within the AAIUI. 
searchEntityAutoSuggestIndex: the index name for storing auto-suggestion data in the search service. Data stored in this index is used in AAIUI to populate VNF search suggestions.
searchAggregationVnfIndex: the index name for storing VNF data in the search service. Data stored in this index is used in AAIUI to populate summary information about VNFs on certain attributes like provisioning status and orchestration status.
searchCertName: the name of the search service certificate file. Synapse looks for this file in the /opt/app/datarouter-service/app-config/auth directory.
searchKeystorePwd: the obsfuscated password for the configured keystore file. This keystore password should be obfuscated using the Jetty password obfuscation tool. 
searchKeystore: the name of the Synapse data router service keystore file. Synapse looks for this file in the /opt/app/datarouter-service/app-config/auth directory.

##### Contents of the /opt/app/datarouter-service/dynamic/routes Directory

The purpose of this configuration directory is to maintain Camel route files specific to _Synapse_ use cases.
The following option files should be present in this directory on the host machine:

_entity-event.route_

In order to take advantage of the entity change event functionality the following configuration Apache Camel route file should be present with the appropriate fields filled in:

	<route xmlns="http://camel.apache.org/schema/spring" trace="true">
	  <from uri="event-bus:{event-bus-client-name}/?eventTopic={event-topic}&amp;groupName={consumer-group-name}&amp;groupId={consumer-group-id}&amp;url={dmaap-url}" />
	  <to uri="bean:entityEventPolicy?method=process"/>
	</route>

Description of configuration fields:

event-bus-client-name: a unique name identifying this consumer of the event bus
event-topic: the name of the topic that this client should consume from
groupName: the consumer group name for this client. The name here should be carefully considered. If this client is added to an existing consumer group, then the client will become responsible for a set of partitions on the topic.
groupId: a unique identifier for this client among the consumer group
dmaap-url: a set of one or more comma separated urls of dmaap servers that this client should attempt to connect to ie: http://dmaap1:3904,http://dmaap2:3904
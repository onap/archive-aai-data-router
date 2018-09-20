# Synapse Micro Service

[![alt text](https://bestpractices.coreinfrastructure.org/projects/1737/badge)](https://bestpractices.coreinfrastructure.org/projects/1737)

## Overview
The Synapse microservice (data router service) acts as a message broker between different sources of information and business logic within the context of AAI. The goal is to provide an extensible framework that can stitch sources of data and business logic together to satisfy use cases through configuration and minimal coding.

Please refer to the following sub-sections for more detailed information:

[High Level Concepts](./CONCEPTS.md) - Discussion of the high level concepts and building blocks of the _SYNAPSE_ Data Router service.

[Synapse Functions](./FUNCTIONS.md) - Discussion of the high level use cases that Synapse can fulfill upon deployment and configuration

## Getting Started

### Building The Micro Service

After cloning the project, execute the following Maven command from the project's top level directory to build the project:

    > mvn clean install

Now, you can build your Docker image:

    > docker build -t onap/datarouter-service target 
    
### Deploying The Micro Service 

Push the Docker image that you have built to your Docker repository and pull it down to the location that you will be running Synapse from.

Note that in order to take advantage of some of the base functionality of Synapse, you will need to have the following:

* optional access to DMaaP in order to pull data off of configured topics
* optional access to a search service installation in order to be able to write indexable data 


**Create the following directories on the host machine:**

    /logs
    /opt/app/datarouter-service/appconfig
    
You will be mounting these as data volumes when you start the Docker container.

**Populate these directories as follows:**

##### Contents of /opt/app/datarouter-service/appconfig

The purpose of this configuration directory is to maintain general configuration files for the _Synapse_ data routing service.
The following files must be present in this directory on the host machine:
    
_data-router.properties_

This file currently requires no configuration but should still remain for future use cases
    
##### Contents of the /opt/app/datarouter-service/app-config/auth Directory

The purpose of this configuration directory is to maintain configuration files specific to authentication/authorization for the _Synapse_ data routing service.
The following files must be present in this directory on the host machine:

_data-router\_policy.json_

Create a policy file defining the roles and users that will be allowed to access _Synapse_ data routing service.  This is a JSON format file which will look something like the following example:

    {
        "roles": [
            {
                "name": "admin",
                "functions": [
                    {
                        "name": "search", "methods": [ { "name": "GET" },{ "name": "DELETE" }, { "name": "PUT" }, { "name": "POST" } ]
                    }
                ],
                "users": [
                    {
                        "username": "CN=synapseadmin, OU=My Organization Unit, O=, L=Sometown, ST=SomeProvince, C=CA"
                    }    
                ]
            }
        ]
    }

_tomcat\_keystore_

Create a keystore with this name containing whatever CA certificates that you want your instance of the _Synapse_ data routing service to accept for HTTPS traffic.

_search-certificate_

Provide a certificate that will allow the _Synapse_ data routing service to communicate with the configured search service via HTTPS

##### Contents of the /opt/app/datarouter-service/app-config/model Directory

The purpose of this configuration directory is to maintain model configuration files for the _Synapse_ data routing service.
The following files must be present in this directory on the host machine:

_aai_oxm_(.*).xml_

Any AAI OXM model files must be placed in the model directory and be named with the aai_oxm_ prefix. ex: aai_oxm_v9.xml. These files are used for use cases that are model driven.

##### Contents of the /opt/app/datarouter-service/dynamic/conf Directory

The purpose of this configuration directory is to maintain configuration spring bean files specific to _Synapse_ use cases. Please refer to [Synapse Functions](./FUNCTIONS.md) for information on specific use cases.


##### Contents of the /opt/app/datarouter-service/dynamic/routes Directory

The purpose of this configuration directory is to maintain Camel route files specific to _Synapse_ use cases.  Please refer to [Synapse Functions](./FUNCTIONS.md) for information on specific use cases.

**Start the service:**

You can now start the Docker container for the _Synapse_ data router service, in the following manner:

	docker run -d \
	    -p 9502:9502 \
		-e CONFIG_HOME=/opt/app/datarouter-service/config/ \
		-e KEY_STORE_PASSWORD=OBF:{{obfuscated password}} \
		-e KEY_MANAGER_PASSWORD=OBF:{{obfuscated password}} \
		-e MAX_HEAP={{jvmmaxheap}} \
	    -v /logs:/opt/aai/logroot/AAI-DR \
	    -v /opt/app/datarouter-service/appconfig:/opt/app/datarouter-service/config \
	    --name datarouter-service \
	    {{your docker repo}}/datarouter-service
    
Where,

    {{your docker repo}} = The Docker repository you have published the Synapse Data Router Service image to.
    {{obfuscated password}} = The password for your key store/key manager after running it through the Jetty obfuscation tool.
    {{jvmmaxheap}} = The max heap size of the JVM for the microservice

 
 
 
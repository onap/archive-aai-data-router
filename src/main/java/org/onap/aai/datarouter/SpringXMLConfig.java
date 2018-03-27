package org.onap.aai.datarouter;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({"file:${SERVICE_BEANS}/*.xml"})
public class SpringXMLConfig {

 
}
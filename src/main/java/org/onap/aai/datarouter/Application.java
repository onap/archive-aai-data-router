/**
 * ﻿============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2017-2018 Amdocs
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.datarouter;

import java.util.HashMap;
import javax.annotation.PostConstruct;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.eclipse.jetty.util.security.Password;
import org.onap.aai.config.EdgesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.FilterType;


@SpringBootApplication
@ComponentScan(basePackages = {"org.onap.aai.config", "org.onap.aai.setup", "org.onap.aai.datarouter"}, excludeFilters = {
@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
value = EdgesConfiguration.class)})
@PropertySource(value = "file:${CONFIG_HOME}/schemaIngest.properties")
public class Application extends SpringBootServletInitializer{

    private static final String CAMEL_URL_MAPPING = "/*";
    private static final String CAMEL_SERVLET_NAME = "CamelServlet";
    private static final String JETTY_OBFUSCATION_PATTERN = "OBF:";

    @Autowired
    private Environment env;

    public static void main(String[] args) {
      String keyStorePassword = System.getenv("KEY_STORE_PASSWORD");
      if(keyStorePassword==null || keyStorePassword.isEmpty()){
        throw new RuntimeException("Env property KEY_STORE_PASSWORD not set");
      }
      
      HashMap<String, Object> props = new HashMap<>();
      String deobfuscatedKeyStorePassword = keyStorePassword.startsWith(JETTY_OBFUSCATION_PATTERN)?Password.deobfuscate(keyStorePassword):keyStorePassword;
      props.put("server.ssl.key-store-password", deobfuscatedKeyStorePassword);
      
      String trustStoreLocation = System.getenv("TRUST_STORE_LOCATION");
      String trustStorePassword = System.getenv("TRUST_STORE_PASSWORD");
      if(trustStoreLocation!=null && trustStorePassword !=null){
          trustStorePassword = trustStorePassword.startsWith(JETTY_OBFUSCATION_PATTERN)?Password.deobfuscate(trustStorePassword):trustStorePassword;
          props.put("server.ssl.trust-store", trustStoreLocation);
          props.put("server.ssl.trust-store-password", trustStorePassword);
      }

      String requireClientAuth = System.getenv("REQUIRE_CLIENT_AUTH");
      if (requireClientAuth == null || requireClientAuth.isEmpty()) {
          props.put("server.ssl.client-auth", "need");
      } else {
          props.put("server.ssl.client-auth", Boolean.valueOf(requireClientAuth)? "need" : "want");
      }

      new Application().configure(new SpringApplicationBuilder(Application.class).properties(props)).run(args);
    }

    @Bean
    public ServletRegistrationBean getServletRegistrationBean() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new CamelHttpTransportServlet(), CAMEL_URL_MAPPING);
        registration.setName(CAMEL_SERVLET_NAME);
        return registration;
    }

    /**
      * Set required system properties using values from application.properties and schemaIngest.properties
     */
    @PostConstruct
    public void setSystemProperties() {
        String trustStorePath = env.getProperty("server.ssl.key-store");
        if (trustStorePath != null) {
            String trustStorePassword = env.getProperty("server.ssl.key-store-password");

            if (trustStorePassword != null) {
                System.setProperty("javax.net.ssl.trustStore", trustStorePath);
                System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
            } else {
                throw new IllegalArgumentException("Env property server.ssl.key-store-password not set");
            }
        }

        String schemaServiceKeyStorePassword = env.getProperty("schema.service.ssl.key-store-password");
        if( (schemaServiceKeyStorePassword != null) && (schemaServiceKeyStorePassword.startsWith(JETTY_OBFUSCATION_PATTERN))){
          System.setProperty("schema.service.ssl.key-store-password", Password.deobfuscate(schemaServiceKeyStorePassword));
        }

        String schemaServiceTrustStorePassword = env.getProperty("schema.service.ssl.trust-store-password");
        if ( (schemaServiceTrustStorePassword != null) && (schemaServiceTrustStorePassword.startsWith(JETTY_OBFUSCATION_PATTERN)) ){
          System.setProperty("schema.service.ssl.trust-store-password", Password.deobfuscate(schemaServiceTrustStorePassword));
        }

    }


}
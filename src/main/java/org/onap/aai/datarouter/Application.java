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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;


@SpringBootApplication
public class Application extends SpringBootServletInitializer{

    private static final String CAMEL_URL_MAPPING = "/*";
    private static final String CAMEL_SERVLET_NAME = "CamelServlet";

    @Autowired
    private Environment env;

    public static void main(String[] args) {
      String keyStorePassword = System.getenv("KEY_STORE_PASSWORD");
      if(keyStorePassword==null || keyStorePassword.isEmpty()){
        throw new RuntimeException("Env property KEY_STORE_PASSWORD not set");
      }
      HashMap<String, Object> props = new HashMap<>();
      props.put("server.ssl.key-store-password", Password.deobfuscate(keyStorePassword));
      new Application().configure(new SpringApplicationBuilder(Application.class).properties(props)).run(args);


    }

    @Bean
    public ServletRegistrationBean getServletRegistrationBean() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new CamelHttpTransportServlet(), CAMEL_URL_MAPPING);
        registration.setName(CAMEL_SERVLET_NAME);
        return registration;
    }

    /**
     * Set required trust store system properties using values from application.properties
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
    }



}
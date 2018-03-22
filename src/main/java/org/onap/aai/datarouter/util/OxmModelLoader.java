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
package org.onap.aai.datarouter.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContextFactory;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.datarouter.logging.DataRouterMsgs;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public class OxmModelLoader {

	private static Map<String, DynamicJAXBContext> versionContextMap = new ConcurrentHashMap<>();
	private static List<ExternalOxmModelProcessor> oxmModelProcessorRegistry = new ArrayList<>();
	static final Pattern p = Pattern.compile("aai_oxm_(.*).xml");

	private static org.onap.aai.cl.api.Logger logger = LoggerFactory.getInstance()
			.getLogger(OxmModelLoader.class.getName());

	public static synchronized void loadModels() throws FileNotFoundException {
	  
    ClassLoader cl = OxmModelLoader.class.getClassLoader();
    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
    Resource[] resources;
    try {
      resources = resolver.getResources("classpath*:/oxm/aai_oxm*.xml");
    } catch (IOException ex) {
      logger.error(DataRouterMsgs.LOAD_OXM_ERROR, ex.getMessage());  
      throw new FileNotFoundException("Unable to load OXM models from schema path : /oxm/aai_oxm*.xml");
    }

    if (resources.length == 0) {
      logger.error(DataRouterMsgs.LOAD_OXM_ERROR, "No OXM schema files found on classpath"); 
      throw new FileNotFoundException("Unable to load OXM models from schema path : /oxm/aai_oxm*.xml");
    }

    for (Resource resource : resources) {
      Matcher matcher = p.matcher(resource.getFilename());

      if (matcher.matches()) {
        try {
          OxmModelLoader.loadModel(matcher.group(1), resource.getFilename(),resource.getInputStream());
        } catch (Exception e) {
          logger.error(DataRouterMsgs.LOAD_OXM_ERROR, "Failed to load " + resource.getFilename()
              + ": " + e.getMessage());          
        }
      }
    }
		
		
	}
	

	private static synchronized void loadModel(String version,String resourceName,InputStream inputStream) throws JAXBException {
		Map<String, Object> properties = new HashMap<>();
		properties.put(JAXBContextProperties.OXM_METADATA_SOURCE, inputStream);
		final DynamicJAXBContext jaxbContext = DynamicJAXBContextFactory
				.createContextFromOXM(Thread.currentThread().getContextClassLoader(), properties);
		versionContextMap.put(version, jaxbContext);
		if ( oxmModelProcessorRegistry != null) {
          for ( ExternalOxmModelProcessor processor : oxmModelProcessorRegistry ) {
             processor.onOxmVersionChange(Version.valueOf(version),  jaxbContext );
          }
         }		
		logger.info(DataRouterMsgs.LOADED_OXM_FILE, resourceName);
	}

	public static DynamicJAXBContext getContextForVersion(String version) throws NoSuchElementException, FileNotFoundException {
		if (versionContextMap == null || versionContextMap.isEmpty()) {
			loadModels();
		} else if (!versionContextMap.containsKey(version)) {			
				throw new NoSuchElementException(Status.NOT_FOUND.toString());
			
		}

		return versionContextMap.get(version);
	}

	public static Map<String, DynamicJAXBContext> getVersionContextMap() {
		return versionContextMap;
	}

	public static void setVersionContextMap(Map<String, DynamicJAXBContext> versionContextMap) {
		OxmModelLoader.versionContextMap = versionContextMap;
	}
	
	public static synchronized void registerExternalOxmModelProcessors(Collection<ExternalOxmModelProcessor> processors) {
      if(processors != null) {
         for(ExternalOxmModelProcessor processor : processors) {
            if(!oxmModelProcessorRegistry.contains(processor)) {
               oxmModelProcessorRegistry.add(processor);
            }
         }
      }
   }

}

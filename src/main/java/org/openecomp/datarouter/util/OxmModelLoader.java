/**
 * ﻿============LICENSE_START=======================================================
 * DataRouter
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property.
 * Copyright © 2017 Amdocs
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 * ECOMP and OpenECOMP are trademarks
 * and service marks of AT&T Intellectual Property.
 */
package org.openecomp.datarouter.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContextFactory;
import org.openecomp.cl.eelf.LoggerFactory;
import org.openecomp.datarouter.logging.DataRouterMsgs;

import org.openecomp.datarouter.util.ExternalOxmModelProcessor;

public class OxmModelLoader {

	private static Map<String, DynamicJAXBContext> versionContextMap = new ConcurrentHashMap<String, DynamicJAXBContext>();
	private static Map<String, Timer> timers = new ConcurrentHashMap<String, Timer>();
	private static List<ExternalOxmModelProcessor> oxmModelProcessorRegistry = new ArrayList<ExternalOxmModelProcessor>();
	final static Pattern p = Pattern.compile("aai_oxm_(.*).xml");
	
	

	private static org.openecomp.cl.api.Logger logger = LoggerFactory.getInstance()
			.getLogger(OxmModelLoader.class.getName());

	public synchronized static void loadModels() {
		
		File[] listOfFiles = new File(DataRouterConstants.DR_HOME_MODEL).listFiles();

		if (listOfFiles != null) {
			for (File file : listOfFiles) {
				if (file.isFile()) {
					Matcher m = p.matcher(file.getName());
					if (m.matches()) {
						try {
							OxmModelLoader.loadModel(m.group(1), file);
						} catch (Exception e) {
							logger.error(DataRouterMsgs.INVALID_OXM_FILE, file.getName(), e.getMessage());
						}
					}

				}
			}
		} else {
			logger.error(DataRouterMsgs.INVALID_OXM_DIR, DataRouterConstants.DR_HOME_MODEL);
		}


	}
	
	private static void addtimer(String version,File file){
		TimerTask task = null;
		task = new FileWatcher(
				file) {
			protected void onChange(File file) {
				// here we implement the onChange
				logger.info(DataRouterMsgs.FILE_CHANGED, file.getName());

				try {
					OxmModelLoader.loadModel(version,file);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		};

		if (!timers.containsKey(version)) {
			Timer timer = new Timer("oxm-"+version);
			timer.schedule(task, new Date(), 10000);
			timers.put(version, timer);

		}
	}

	private synchronized static void loadModel(String version,File file) throws JAXBException, FileNotFoundException {

		
		InputStream iStream = new FileInputStream(file);
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(JAXBContextProperties.OXM_METADATA_SOURCE, iStream);
		final DynamicJAXBContext jaxbContext = DynamicJAXBContextFactory
				.createContextFromOXM(Thread.currentThread().getContextClassLoader(), properties);
		versionContextMap.put(version, jaxbContext);
		if ( oxmModelProcessorRegistry != null) {
          for ( ExternalOxmModelProcessor processor : oxmModelProcessorRegistry ) {
             processor.onOxmVersionChange(Version.valueOf(version),  jaxbContext );
          }
         }
		addtimer(version,file);

	}

	public static DynamicJAXBContext getContextForVersion(String version) throws Exception {
		if (versionContextMap == null || versionContextMap.isEmpty()) {
			loadModels();
		} else if (!versionContextMap.containsKey(version)) {
			try {
				loadModel(version,new File (DataRouterConstants.DR_HOME_MODEL + "aai_oxm_" + version + ".xml"));
			} catch (Exception e) {
				throw new Exception(Status.NOT_FOUND.toString());
			}
		}

		return versionContextMap.get(version);
	}

	public static Map<String, DynamicJAXBContext> getVersionContextMap() {
		return versionContextMap;
	}

	public static void setVersionContextMap(Map<String, DynamicJAXBContext> versionContextMap) {
		OxmModelLoader.versionContextMap = versionContextMap;
	}
	
	public synchronized static void registerExternalOxmModelProcessors(Collection<ExternalOxmModelProcessor> processors) {
      if(processors != null) {
         for(ExternalOxmModelProcessor processor : processors) {
            if(!oxmModelProcessorRegistry.contains(processor)) {
               oxmModelProcessorRegistry.add(processor);
            }
         }
      }
   }

}

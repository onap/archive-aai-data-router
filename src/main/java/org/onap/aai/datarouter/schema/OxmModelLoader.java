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
package org.onap.aai.datarouter.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import javax.ws.rs.core.Response.Status;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.datarouter.logging.DataRouterMsgs;
import org.onap.aai.datarouter.util.ExternalOxmModelProcessor;
import org.onap.aai.nodes.NodeIngestor;
import org.onap.aai.setup.ConfigTranslator;
import org.onap.aai.setup.SchemaLocationsBean;
import org.onap.aai.setup.Version;

public class OxmModelLoader {

    private static Map<String, DynamicJAXBContext> versionContextMap = new ConcurrentHashMap<>();
    private static List<ExternalOxmModelProcessor> oxmModelProcessorRegistry = new ArrayList<>();

    private static final org.onap.aai.cl.api.Logger logger =
            LoggerFactory.getInstance().getLogger(OxmModelLoader.class.getName());

    private OxmModelLoader() {
        throw new IllegalStateException("Utility class");
    }

    public static synchronized void loadModels() {
        SchemaIngestPropertiesReader schemaIngestPropReader = new SchemaIngestPropertiesReader();
        SchemaLocationsBean schemaLocationsBean = new SchemaLocationsBean();
        schemaLocationsBean.setNodeDirectory(schemaIngestPropReader.getNodeDir());
        ConfigTranslator configTranslator = new OxmConfigTranslator(schemaLocationsBean);
        NodeIngestor nodeIngestor = new NodeIngestor(configTranslator);

        for (Version oxmVersion : Version.values()) {
            DynamicJAXBContext jaxbContext = nodeIngestor.getContextForVersion(oxmVersion);
            if (jaxbContext != null) {
                loadModel(oxmVersion.toString(), jaxbContext);
            }
        }
    }

    public static DynamicJAXBContext getContextForVersion(String version) {
        if (versionContextMap == null || versionContextMap.isEmpty()) {
            loadModels();
        } else if (!versionContextMap.containsKey(version)) {
            throw new NoSuchElementException(Status.NOT_FOUND.toString());
        }
        return versionContextMap.get(version);
    }

    public static synchronized void registerExternalOxmModelProcessors(
            Collection<ExternalOxmModelProcessor> processors) {
        if (processors != null) {
            for (ExternalOxmModelProcessor processor : processors) {
                if (!oxmModelProcessorRegistry.contains(processor)) {
                    oxmModelProcessorRegistry.add(processor);
                }
            }
        }
    }

    public static Map<String, DynamicJAXBContext> getVersionContextMap() {
        return versionContextMap;
    }

    private static synchronized void loadModel(String oxmVersion, DynamicJAXBContext jaxbContext) {
        versionContextMap.put(oxmVersion, jaxbContext);
        if (oxmModelProcessorRegistry != null) {
            for (ExternalOxmModelProcessor processor : oxmModelProcessorRegistry) {
                processor.onOxmVersionChange(org.onap.aai.datarouter.util.Version.valueOf(oxmVersion.toLowerCase()),
                        jaxbContext);
            }
        }
        logger.info(DataRouterMsgs.LOADED_OXM_FILE, oxmVersion);
    }

}

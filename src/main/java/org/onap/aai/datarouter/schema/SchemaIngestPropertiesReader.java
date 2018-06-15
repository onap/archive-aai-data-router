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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.datarouter.logging.DataRouterMsgs;

public class SchemaIngestPropertiesReader {

    private static final String SCHEMA_INGEST_PROPERTIES_FILE = "schemaIngest.properties";
    private static final String MSG_FAILED_LOAD_SCHEMA_INGEST_PROPERTIES = "Failed to load " + SCHEMA_INGEST_PROPERTIES_FILE;

    private static final String SCHEMA_INGEST_PROPERTIES_LOCATION =
            System.getProperty("CONFIG_HOME") + "/" + SCHEMA_INGEST_PROPERTIES_FILE;

    private static org.onap.aai.cl.api.Logger logger =
            LoggerFactory.getInstance().getLogger(SchemaIngestPropertiesReader.class.getName());

    /**
     * Gets the location of the OXM.
     *
     * @return the directory containing the OXM files
     */
    public String getNodeDir() {

        Properties prop = new Properties();
        try {
            prop = loadFromFile(SCHEMA_INGEST_PROPERTIES_LOCATION);
        } catch (NoSuchFileException e) {
            try {
                prop = loadFromClasspath(SCHEMA_INGEST_PROPERTIES_FILE);
            } catch (URISyntaxException | IOException ef) {
                logger.error(DataRouterMsgs.SCHEMA_INGEST_LOAD_ERROR, ef.getMessage());
                throw new RuntimeException(MSG_FAILED_LOAD_SCHEMA_INGEST_PROPERTIES);
            }
        } catch (IOException ec) {
            logger.error(DataRouterMsgs.SCHEMA_INGEST_LOAD_ERROR, ec.getMessage());
            throw new RuntimeException(MSG_FAILED_LOAD_SCHEMA_INGEST_PROPERTIES);
        }
        return prop.getProperty("nodeDir");
    }

    private Properties loadFromFile(String filename) throws IOException {
        Path configLocation = Paths.get(filename);
        try (InputStream stream = Files.newInputStream(configLocation)) {
            return loadProperties(stream);
        }
    }

    private Properties loadFromClasspath(String resourceName) throws URISyntaxException, IOException {
        Path path = Paths.get(ClassLoader.getSystemResource(resourceName).toURI());
        try (InputStream stream = Files.newInputStream(path)) {
            return loadProperties(stream);
        }
    }

    private Properties loadProperties(InputStream stream) throws IOException {
        Properties config = new Properties();
        config.load(stream);
        return config;
    }
}

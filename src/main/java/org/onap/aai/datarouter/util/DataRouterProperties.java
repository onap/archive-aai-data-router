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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.datarouter.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataRouterProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataRouterProperties.class);

    private static Properties properties;

    static {
        File file = new File(DataRouterConstants.DR_CONFIG_FILE);
        loadProperties(file);
    }

    static void loadProperties(File file) {
        properties = new Properties();
        try (InputStream props = new FileInputStream(file)) {
            properties.load(props);
        } catch (FileNotFoundException e) {
            LOGGER.error("FileNotFoundException: ", e);
        } catch (IOException e) {
            LOGGER.error("IOException: ", e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

}

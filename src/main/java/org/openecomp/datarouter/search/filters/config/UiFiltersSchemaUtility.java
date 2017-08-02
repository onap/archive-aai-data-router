/**
 * ﻿============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2017 Amdocs
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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */
package org.openecomp.datarouter.search.filters.config;

import java.io.File;
import java.io.IOException;

import org.openecomp.cl.api.Logger;
import org.openecomp.cl.eelf.LoggerFactory;
import org.openecomp.datarouter.logging.DataRouterMsgs;
import org.openecomp.datarouter.util.DataRouterConstants;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility methods for interacting with the UI filters schema file
 */
public class UiFiltersSchemaUtility {
  private static final Logger LOG =
      LoggerFactory.getInstance().getLogger(UiFiltersSchemaUtility.class);

  private ObjectMapper mapper = new ObjectMapper();

  /**
   * Reads in the file and populates an object with that data
   * 
   * @throws Exception
   */
  public UiFiltersConfig loadUiFiltersConfig() {
    UiFiltersConfig filtersConfig = new UiFiltersConfig();

    try {
      filtersConfig = mapper.readValue(new File(DataRouterConstants.UI_FILTER_LIST_FILE), UiFiltersConfig.class);
    } catch (IOException e) {
      LOG.error(DataRouterMsgs.JSON_CONVERSION_ERROR, "Could not convert filters config file " +
          DataRouterConstants.UI_FILTER_LIST_FILE + " to " + filtersConfig.getClass());
    }

    return filtersConfig;
  }
}

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

public class DataRouterConstants {
  public static final String DR_FILESEP = (System.getProperty("file.separator") == null) ? "/"
      : System.getProperty("file.separator");
  
  public static final String DR_AJSC_HOME = System.getProperty("AJSC_HOME");

  public static final String DR_SPECIFIC_CONFIG = System.getProperty("CONFIG_HOME") + DR_FILESEP;
  
  public static final String DR_BUNDLECONFIG_NAME = (System.getProperty("BUNDLECONFIG_DIR") == null)
      ? "bundleconfig" : System.getProperty("BUNDLECONFIG_DIR");

  public static final String DR_HOME_BUNDLECONFIG = (DR_AJSC_HOME == null)
      ? DR_FILESEP + "opt" + DR_FILESEP + "app" + DR_FILESEP 
          + "datalayer" + DR_FILESEP + DR_BUNDLECONFIG_NAME
      : DR_AJSC_HOME + DR_FILESEP + DR_BUNDLECONFIG_NAME;

  /** This is the etc directory, relative to AAI_HOME. */
  public static final String DR_HOME_ETC = DR_HOME_BUNDLECONFIG + DR_FILESEP + "etc" + DR_FILESEP;

  public static final String DR_HOME_MODEL = DR_SPECIFIC_CONFIG + "model" + DR_FILESEP;
  public static final String DR_HOME_AUTH = DR_SPECIFIC_CONFIG + "auth" + DR_FILESEP;

  public static final String DR_CONFIG_FILE = DR_SPECIFIC_CONFIG + "data-router.properties";

  public static final String DR_HOME_ETC_OXM = DR_HOME_ETC + "oxm" + DR_FILESEP;
  
  public static final String UI_FILTER_LIST_FILE =
      DR_SPECIFIC_CONFIG + "filters" + DR_FILESEP + "aaiui_filters.json";

  // AAI Related
  public static final String AAI_ECHO_SERVICE = "/util/echo";

  // Logging related
  public static final String DATA_ROUTER_SERVICE_NAME = "DataRouter";
}

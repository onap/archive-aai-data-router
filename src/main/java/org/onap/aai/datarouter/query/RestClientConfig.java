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
package org.onap.aai.datarouter.query;

public class RestClientConfig {

  private String certPath;
  private String certPassword;
  private String trustStorePath;
  private String connectionTimeout;
  private String readTimeout;

  public String getCertPath() {
    return certPath;
  }

  public void setCertPath(String certPath) {
    this.certPath = certPath;
  }

  public String getCertPassword() {
    return certPassword;
  }

  public void setCertPassword(String certPassword) {
    this.certPassword = certPassword;
  }

  public String getTrustStorePath() {
    return trustStorePath;
  }

  public void setTrustStorePath(String trustStorePath) {
    this.trustStorePath = trustStorePath;
  }

  public int getConnectionTimeout() {
    return parseInt(connectionTimeout,10000);
  }

  public void setConnectionTimeout(String connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  public int getReadTimeout() {
    return parseInt(connectionTimeout,10000);
  }

  public void setReadTimeout(String readTimeout) {
    this.readTimeout = readTimeout;
  }

  
  private int parseInt(String config, int defaultValue) {
    int intVal = defaultValue; // Default delay of half a sec
    try {
      intVal = Integer.parseInt(config);
    } catch (Exception e) {
      // Ignore the parsing error and use the default
    }
    return intVal;
  }
  @Override
  public String toString() {
    return "RestClientConfig [certPath=" + certPath + ", certPassword=" + certPassword + ", trustStorePath="
        + trustStorePath + ", connectionTimeout=" + connectionTimeout + ", readTimeout=" + readTimeout + "]";
  }

}

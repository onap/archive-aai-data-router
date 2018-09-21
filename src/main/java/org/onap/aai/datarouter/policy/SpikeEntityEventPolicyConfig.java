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
package org.onap.aai.datarouter.policy;

import org.onap.aai.setup.SchemaLocationsBean;
import org.onap.aai.setup.SchemaVersions;

public class SpikeEntityEventPolicyConfig {

  private String sourceDomain;
  private String searchBaseUrl;
  private String searchEndpoint;
  private String searchEndpointDocuments;
  private String searchEntitySearchIndex;
  private String searchCertName;
  private String searchKeystorePwd;
  private String searchKeystore;
  private SchemaVersions schemaVersions;
  private SchemaLocationsBean schemaLocationsBean;
  
  public String getSourceDomain() {
    return sourceDomain;
  }
  
  public void setSourceDomain(String sourceDomain) {
    this.sourceDomain = sourceDomain;
  }
  
  public String getSearchBaseUrl() {
    return searchBaseUrl;
  }
  
  public void setSearchBaseUrl(String searchBaseUrl) {
    this.searchBaseUrl = searchBaseUrl;
  }
  
  public String getSearchEndpoint() {
    return searchEndpoint;
  }
  
  public void setSearchEndpoint(String searchEndpoint) {
    this.searchEndpoint = searchEndpoint;
  }
  
  public String getSearchEndpointDocuments() {
    return searchEndpointDocuments;
  }
  
  public void setSearchEndpointDocuments(String searchEndpointDocuments) {
    this.searchEndpointDocuments = searchEndpointDocuments;
  }
  
  public String getSearchEntitySearchIndex() {
    return searchEntitySearchIndex;
  }
  
  public void setSearchEntitySearchIndex(String searchEntitySearchIndex) {
    this.searchEntitySearchIndex = searchEntitySearchIndex;
  }
  public String getSearchCertName() {
    return searchCertName;
  }
  
  public void setSearchCertName(String searchCertName) {
    this.searchCertName = searchCertName;
  }
  
  public String getSearchKeystore() {
    return searchKeystore;
  }
  
  public void setSearchKeystore(String searchKeystore) {
    this.searchKeystore = searchKeystore;
  }
  
  public String getSearchKeystorePwd() {
    return searchKeystorePwd;
  }
  
  public void setSearchKeystorePwd(String searchKeystorePwd) {
    this.searchKeystorePwd = searchKeystorePwd;
  } 
  
  public SchemaVersions getSchemaVersions() {
    return schemaVersions;
  }
  
  public void setSchemaVersions(SchemaVersions schemaVersions) {
    this.schemaVersions = schemaVersions;
  }
  
  public SchemaLocationsBean getSchemaLocationsBean() {
    return schemaLocationsBean;
  }
  
  public void setSchemaLocationsBean(SchemaLocationsBean schemaLocationsBean) {
    this.schemaLocationsBean = schemaLocationsBean;
  }
}

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
package org.onap.aai.datarouter.search.filters.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class UiFilterDataSourceConfig {
 
  @JsonProperty("indexName")
  private String indexName;
  
  @JsonProperty("docType")
  private String docType;
  
  @JsonProperty("fieldName")
  private String fieldName;
  
  @JsonProperty("pathToField")
  private String pathToField;
 
  public UiFilterDataSourceConfig(){}
  
  @JsonCreator
  public UiFilterDataSourceConfig(
      @JsonProperty("indexName") final String indexName, @JsonProperty("docType") final String docType,
      @JsonProperty("fieldName") final String fieldName, @JsonProperty("pathToField") final String pathToField) {
    this.indexName = indexName;
    this.docType = docType;
    this.fieldName = fieldName;
    this.pathToField = pathToField;
  }

  @JsonProperty("indexName")
  public String getIndexName() {
    return indexName;
  }

  public void setIndexName(String indexName) {
    this.indexName = indexName;
  }

  @JsonProperty("docType")
  public String getDocType() {
    return docType;
  }

  public void setDocType(String docType) {
    this.docType = docType;
  }

  @JsonProperty("fieldName")
  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }
  
  @JsonProperty("pathToField")
  public String getPathToField() {
    return pathToField;
  }
  
  public void setPathToField(String pathToField) {
    this.pathToField = pathToField;
  }

  @Override
  public String toString() {
    return "UiFilterDataSourceConfig [indexName=" + indexName + ", docType=" + docType
        + ", fieldName=" + fieldName + ", pathToField=" + pathToField + "]";
  }
}


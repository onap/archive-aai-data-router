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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class UiFilterConfig {

  @JsonProperty("filterId")
  private String filterId;

  @JsonProperty("filterName")
  private String filterName;

  @JsonProperty("displayName")
  private String displayName;

  @JsonProperty("dataType")
  private String dataType;

  @JsonProperty("dataSource")
  private UiFilterDataSourceConfig dataSource = new UiFilterDataSourceConfig();

  @JsonCreator
  public UiFilterConfig(@JsonProperty("filterId") final String filterId,
      @JsonProperty("filterName") final String filterName,
      @JsonProperty("displayName") final String displayName,
      @JsonProperty("dataType") final String dataType,
      @JsonProperty("dataSource") final UiFilterDataSourceConfig dataSource
      ) {
    this.filterId = filterId;
    this.filterName = filterName;
    this.displayName = displayName;
    this.dataType = dataType;
    this.dataSource = dataSource;
  }

  @JsonProperty("filterId")
  public String getFilterId() {
    return filterId;
  }

  public void setFilterId(String filterId) {
    this.filterId = filterId;
  }

  @JsonProperty("filterName")
  public String getFilterName() {
    return filterName;
  }

  public void setFilterName(String filterName) {
    this.filterName = filterName;
  }

  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  @JsonProperty("dataType")
  public String getDataType() {
    return dataType;
  }

  public void setDataType(String dataType) {
    this.dataType = dataType;
  }

  @JsonProperty("dataSource")
  public UiFilterDataSourceConfig getDataSource() {
    return dataSource;
  }

  public void setDataSource(UiFilterDataSourceConfig dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public String toString() {
    return "UiFilterConfig [filterId=" + filterId + ", filterName=" + filterName + ", displayName="
        + displayName + ", dataType=" + dataType + ", dataSource=" + dataSource + "]";
  }
}

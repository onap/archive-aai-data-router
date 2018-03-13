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
package org.onap.aai.datarouter.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A convenience POJO for mapping the Vertex from a Spike Event.
 * 
 * @author salmaA
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpikeEventVertex {

  private String key;

  private String schemaVersion;

  private String type;  


 
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getSchemaVersion() {
    return schemaVersion;
  }

  public void setSchemaVersion(String schemaVersion) {
    this.schemaVersion = schemaVersion;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
  
  public String getEntityLink() {
    return this.type + "/" + this.key; 
  }
 
}

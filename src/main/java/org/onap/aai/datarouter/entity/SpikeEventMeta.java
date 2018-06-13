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

import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.json.JSONObject;

public class SpikeEventMeta {

  private JSONObject eventEntity;
  private JSONObject eventHeader;
  private JSONObject eventBody;
  private JSONObject spikeVertex;
  private JSONObject vertexProperties;
  private SpikeEventVertex spikeEventVertex;
  private DynamicJAXBContext oxmJaxbContext;
  private String bodyOperationType;

  public JSONObject getEventEntity() {
    return eventEntity;
  }

  public void setEventEntity(JSONObject eventEntity) {
    this.eventEntity = eventEntity;
  }

  public JSONObject getEventHeader() {
    return eventHeader;
  }

  public void setEventHeader(JSONObject eventHeader) {
    this.eventHeader = eventHeader;
  }

  public JSONObject getEventBody() {
    return eventBody;
  }

  public void setEventBody(JSONObject eventBody) {
    this.eventBody = eventBody;
  }

  public JSONObject getSpikeVertex() {
    return spikeVertex;
  }

  public void setSpikeVertex(JSONObject spikeVertex) {
    this.spikeVertex = spikeVertex;
  }

  public JSONObject getVertexProperties() {
    return vertexProperties;
  }

  public void setVertexProperties(JSONObject vertexProperties) {
    this.vertexProperties = vertexProperties;
  }

  public SpikeEventVertex getSpikeEventVertex() {
    return spikeEventVertex;
  }

  public void setSpikeEventVertex(SpikeEventVertex spikeEventVertex) {
    this.spikeEventVertex = spikeEventVertex;
  }

  public DynamicJAXBContext getOxmJaxbContext() {
    return oxmJaxbContext;
  }

  public void setOxmJaxbContext(DynamicJAXBContext oxmJaxbContext) {
    this.oxmJaxbContext = oxmJaxbContext;
  }

  public String getBodyOperationType() {
    return bodyOperationType;
  }

  public void setBodyOperationType(String bodyOperationType) {
    this.bodyOperationType = bodyOperationType;
  }
  
  
  

}

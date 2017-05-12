/**
 * ﻿============LICENSE_START=======================================================
 * DataRouter
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property.
 * Copyright © 2017 Amdocs
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 * ECOMP and OpenECOMP are trademarks
 * and service marks of AT&T Intellectual Property.
 */
package org.openecomp.datarouter.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A convenience POJO for mapping the UebEventHeader from a UEB Event.
 * 
 * @author davea
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class UebEventHeader {

  private String timestamp;

  private String id;

  private String action;

  private String domain;

  private String sourceName;

  private String entityLink;

  private String entityType;

  private String topEntityType;

  private String sequenceNumber;

  private String eventType;

  private String version;

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getSourceName() {
    return sourceName;
  }

  @JsonProperty("source-name")
  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }

  public String getEntityLink() {
    return entityLink;
  }

  @JsonProperty("entity-link")
  public void setEntityLink(String entityLink) {
    this.entityLink = entityLink;
  }

  public String getEntityType() {
    return entityType;
  }

  @JsonProperty("entity-type")
  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public String getTopEntityType() {
    return topEntityType;
  }

  @JsonProperty("top-entity-type")
  public void setTopEntityType(String topEntityType) {
    this.topEntityType = topEntityType;
  }

  public String getSequenceNumber() {
    return sequenceNumber;
  }

  @JsonProperty("sequence-number")
  public void setSequenceNumber(String sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }

  public String getEventType() {
    return eventType;
  }

  @JsonProperty("event-type")
  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public String toString() {
    return "UebEventHeader [" + (timestamp != null ? "timestamp=" + timestamp + ", " : "")
        + (id != null ? "id=" + id + ", " : "") + (action != null ? "action=" + action + ", " : "")
        + (domain != null ? "domain=" + domain + ", " : "")
        + (sourceName != null ? "sourceName=" + sourceName + ", " : "")
        + (entityLink != null ? "entityLink=" + entityLink + ", " : "")
        + (entityType != null ? "entityType=" + entityType + ", " : "")
        + (topEntityType != null ? "topEntityType=" + topEntityType + ", " : "")
        + (sequenceNumber != null ? "sequenceNumber=" + sequenceNumber + ", " : "")
        + (eventType != null ? "eventType=" + eventType + ", " : "")
        + (version != null ? "version=" + version : "") + "]";
  }

}

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

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.onap.aai.datarouter.util.NodeUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class AggregationEntity. Mimics functionality of AAIUI's AggregationEntity
 */
public class AggregationEntity implements DocumentStoreDataEntity, Serializable {
  private String id;
  private String link;
  private String lastmodTimestamp;
  
  public String getLink() {
    return link;
  }
  public void setLink(String link) {
    this.link = link;
  }

  @Override
  public String getId() {
    // make sure that deliveFields() is called before getting the id
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }


  public String getLastmodTimestamp() {
    return lastmodTimestamp;
  }
  public void setLastmodTimestamp(String lastmodTimestamp) {
    this.lastmodTimestamp = lastmodTimestamp;
  }


  Map<String, String> attributes = new HashMap<>();
  ObjectMapper mapper = new ObjectMapper();
  
  /**
   * Instantiates a new aggregation entity.
   */
  public AggregationEntity() {  }

  public void deriveFields(JsonNode uebPayload) {
    
    this.setId(NodeUtils.generateUniqueShaDigest(link));
    
    this.setLastmodTimestamp(Long.toString(System.currentTimeMillis()));
    
    JsonNode entityNode = uebPayload.get("entity");
    
    Iterator<Entry<String, JsonNode>> nodes = entityNode.fields();

    while (nodes.hasNext()) {
      Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) nodes.next();
      if (!"relationship-list".equalsIgnoreCase(entry.getKey())){
        attributes.put(entry.getKey(), entry.getValue().asText());
      }
    }
  }
  
  public void copyAttributeKeyValuePair(Map<String, Object> map){
    for(Map.Entry<String, Object> entry: map.entrySet()){
      String key = entry.getKey();
      Object value = entry.getValue();
      if (!"relationship-list".equalsIgnoreCase(key)){   // ignore relationship data which is not required in aggregation
        this.attributes.put(key, value.toString());    // not sure if entity attribute can contain an object as value
      }
    }
  }
 
  public void addAttributeKeyValuePair(String key, String value){
    this.attributes.put(key, value);
  }
  @Override
  public String getAsJson() {
    ObjectNode rootNode = mapper.createObjectNode();
    rootNode.put("link", this.getLink());
    rootNode.put("lastmodTimestamp", lastmodTimestamp);
    for (Map.Entry<String, String> entry: this.attributes.entrySet()){
      String key = entry.getKey();
      String value = entry.getValue();
      rootNode.put(key, value);
    }
    return rootNode.toString();
  }
  
  @Override
  public String toString() {
    return "AggregationEntity [id=" + id + ", link=" + link + ", attributes=" + attributes
        + ", mapper=" + mapper + "]";
  }
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ChameleonResponseBuiler  {

  private static final String SOURCE = "source";
  private static final String TARGET = "target";
  private static final String TYPE = "type";

  public static void buildEntity(Exchange exchange, String id){
    String response = exchange.getIn().getBody().toString();
    JsonParser parser = new JsonParser();
    JsonObject root = parser.parse(response).getAsJsonObject();
    JsonObject champResponse = new JsonObject();
    if (!root.has(TYPE)) {
      exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
      return ;
    }
    champResponse.addProperty("key", id);
    champResponse.addProperty(TYPE, root.get(TYPE).getAsString());
    if (root.has(SOURCE)) {
      champResponse.add(SOURCE, root.get(SOURCE));
    }
    if (root.has(TARGET)) {
      champResponse.add(TARGET, root.get(TARGET));
    }

    JsonObject props = new JsonObject();
    List<Map.Entry<String, JsonElement>> entries = new ArrayList<Map.Entry<String, JsonElement>>(
        root.getAsJsonObject().entrySet());
    for (Map.Entry<String, JsonElement> e : entries) {
      if (!TYPE.equals(e.getKey()) && !SOURCE.equals(e.getKey()) && !TARGET.equals(e.getKey())) {
        props.addProperty(e.getKey(), e.getValue().getAsString());
      }

    }
    
    champResponse.add("properties", props);

    exchange.getIn().setBody(champResponse.toString());
    
  }
  
 
  public static void buildObjectRelationship(Exchange exchange, String id){
    //TODO: implement when chameleon supports this query     
  }
  public static void buildCollection(Exchange exchange){
    //TODO: implement when chameleon supports this query   
  }
  
 
}

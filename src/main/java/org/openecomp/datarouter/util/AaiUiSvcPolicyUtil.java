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
package org.openecomp.datarouter.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class AaiUiSvcPolicyUtil {

  static ObjectMapper mapper = new ObjectMapper();
  
  public static JsonNode getOriginPayload(JsonNode payload) throws Exception{
    /*
     *{
        "origin-uri": "/routerService/1search1",
        "origin-payload": {}
      }
     */
    JsonNode origPayload = null;
    
    if (payload.has("origin-payload")){
      origPayload = payload.get("origin-payload");
    }
    return origPayload;
  }
  
  public static String getOriginUri ( JsonNode payload ) throws Exception {
    String originUri = "";
    if (payload.has("origin-uri")){
      originUri = payload.get("origin-uri").textValue();
    }
    return originUri;
  }
  
  public static String getTargetUri(JsonNode payload) throws Exception{
    /*
     *{
        "origin-uri": "/routerService/1search1",
        "origin-payload": {}
      }
     */
    String uri = "";
    String originUri = getOriginUri(payload);
    final Matcher m = Pattern.compile("/routerService/(.*)").matcher(originUri);
    if ( m.find() ) {
      uri = m.group(1);
    } 
    return uri;
  }
  
}

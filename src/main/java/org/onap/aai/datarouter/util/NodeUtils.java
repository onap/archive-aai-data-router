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
package org.onap.aai.datarouter.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.datarouter.logging.EntityEventPolicyMsgs;

public class NodeUtils {

  private NodeUtils() {

  }

  /**
  * Generate unique sha digest. This method is copy over from NodeUtils class in AAIUI
  *
  * @param keys the keys
  * @return the string
  */
  public static String generateUniqueShaDigest(String... keys) {
    if ((keys == null) || keys.length == 0) {
      return null;
    }
  
    final String keysStr = Arrays.asList(keys).toString();
    final String hashedId = org.apache.commons.codec.digest.DigestUtils.sha256Hex(keysStr);
  
    return hashedId;
  }
  
  /**
   * Extract field value from object.
   *
   * @param node the node
   * @param fieldName the field name
   * @return the string
   */
  public static String extractFieldValueFromObject(JsonNode node, String fieldName) {

    if (node == null) {
      return null;
    }

    if (node.isObject()) {

      JsonNode valueNode = node.get(fieldName);

      if (valueNode != null) {

        if (valueNode.isValueNode()) {
          return valueNode.asText();
        }
      }

    }
    return null;

  }

  /**
   * Convert json str to json node.
   *
   * @param jsonStr the json str
   * @return the json node
   */
  public static JsonNode convertJsonStringToJsonNode(String jsonStr, Logger logger) {
    if (jsonStr == null || jsonStr.isEmpty()) {
      return null;
    }
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonNode = null;
    try {
      jsonNode = mapper.readTree(jsonStr);
    } catch (IOException e) {
      if (logger != null) {
        logger.debug(EntityEventPolicyMsgs.FAILED_TO_PARSE_UEB_PAYLOAD, e.getMessage());
        logger.error(EntityEventPolicyMsgs.FAILED_TO_PARSE_UEB_PAYLOAD, e.getMessage());
      }
    }
    return jsonNode;
  }

  /**
   * Extract objects by key.
   *
   * @param node the node
   * @param searchKey the search key
   * @param foundObjects the found objects
   */
  public static void extractObjectsByKey(JsonNode node, String searchKey,
      Collection<JsonNode> foundObjects) {

    if ( node == null ) {
      return;
    }
    
    if (node.isObject()) {
      Iterator<Map.Entry<String, JsonNode>> nodeIterator = node.fields();

      while (nodeIterator.hasNext()) {
        Map.Entry<String, JsonNode> entry = nodeIterator.next();
        if (!entry.getValue().isValueNode()) {
          extractObjectsByKey(entry.getValue(), searchKey, foundObjects);
        }

        String name = entry.getKey();
        if (name.equalsIgnoreCase(searchKey)) {

          JsonNode entryNode = entry.getValue();

          if (entryNode.isArray()) {

            Iterator<JsonNode> arrayItemsIterator = entryNode.elements();
            while (arrayItemsIterator.hasNext()) {
              foundObjects.add(arrayItemsIterator.next());
            }

          } else {
            foundObjects.add(entry.getValue());
          }


        }
      }
    } else if (node.isArray()) {
      Iterator<JsonNode> arrayItemsIterator = node.elements();
      while (arrayItemsIterator.hasNext()) {
        extractObjectsByKey(arrayItemsIterator.next(), searchKey, foundObjects);
      }

    }

  }
  
  /**
   * Convert object to json.
   *
   * @param object the object
   * @param pretty the pretty
   * @return the string
   * @throws JsonProcessingException the json processing exception
   */
  public static String convertObjectToJson(Object object, boolean pretty)
      throws JsonProcessingException {
    ObjectWriter ow;

    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    
    if (pretty) {
      ow = mapper.writer().withDefaultPrettyPrinter();

    } else {
      ow = mapper.writer();
    }

    return ow.writeValueAsString(object);
  }

  /**
   * Load the UEB JSON payload, any errors would result to a failure case response.
   * @param payload the Payload
   * @param contentKey the Content key
   * @param logger the Logger
   * @return UEB JSON content
   */
  public static JSONObject getUebContentAsJson(String payload, String contentKey, Logger logger) {

    JSONObject uebJsonObj;
    JSONObject uebObjContent;

    try {
      uebJsonObj = new JSONObject(payload);
    } catch (JSONException e) {
      logger.debug(EntityEventPolicyMsgs.UEB_INVALID_PAYLOAD_JSON_FORMAT, payload);
      logger.error(EntityEventPolicyMsgs.UEB_INVALID_PAYLOAD_JSON_FORMAT, payload);
      return null;
    }

    if (uebJsonObj.has(contentKey)) {
      uebObjContent = uebJsonObj.getJSONObject(contentKey);
    } else {
      logger.debug(EntityEventPolicyMsgs.UEB_FAILED_TO_PARSE_PAYLOAD, contentKey);
      logger.error(EntityEventPolicyMsgs.UEB_FAILED_TO_PARSE_PAYLOAD, contentKey);
      return null;
    }

    return uebObjContent;
  }


}

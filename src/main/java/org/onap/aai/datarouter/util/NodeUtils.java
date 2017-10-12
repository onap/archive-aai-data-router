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

public class NodeUtils {
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
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static JsonNode convertJsonStrToJsonNode(String jsonStr) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    if (jsonStr == null || jsonStr.length() == 0) {
      return null;
    }

    return mapper.readTree(jsonStr);
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

}

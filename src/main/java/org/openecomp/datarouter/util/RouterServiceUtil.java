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

import com.fasterxml.jackson.databind.JsonNode;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RouterServiceUtil {

  public static Map<String, String> parseJsonPayloadIntoMap(String jsonPayload) {

    JSONObject jsonObject = new JSONObject(jsonPayload);
    Map<String, String> map = new HashMap<String, String>();
    Iterator iter = jsonObject.keys();
    while (iter.hasNext()) {
      String key = (String) iter.next();
      String value = jsonObject.getString(key);
      map.put(key, value);
    }
    return map;
  }

  public static String getNodeFieldAsText(JsonNode node, String fieldName) {

    String fieldValue = null;

    JsonNode valueNode = node.get(fieldName);

    if (valueNode != null) {
      fieldValue = valueNode.asText();
    }

    return fieldValue;
  }

  public static String concatArray(List<String> list) {
    return concatArray(list, " ");
  }

  public static String concatArray(List<String> list, String delimiter) {

    if (list == null || list.size() == 0) {
      return "";
    }

    StringBuilder result = new StringBuilder(64);

    boolean firstValue = true;

    for (String item : list) {

      if (firstValue) {
        result.append(item);
        firstValue = false;
      } else {
        result.append(delimiter).append(item);
      }
    }

    return result.toString();

  }

  public static String concatArray(String[] values) {

    if (values == null || values.length == 0) {
      return "";
    }

    StringBuilder result = new StringBuilder(64);

    boolean firstValue = true;

    for (String item : values) {

      if (firstValue) {
        result.append(item);
        firstValue = false;
      } else {
        result.append(".").append(item);
      }

    }

    return result.toString();

  }

  public static String recursivelyLookupJsonPayload(JsonNode node, String key) {
    String value = null;
    if (node.isObject()) {
      Iterator<Map.Entry<String, JsonNode>> nodeIterator = node.fields();

      while (nodeIterator.hasNext()) {
        Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) nodeIterator.next();
        if (!entry.getValue().isValueNode()) {
          value = recursivelyLookupJsonPayload(entry.getValue(), key);
          if (value != null) {
            return value;
          }
        }

        String name = entry.getKey();
        if (name.equalsIgnoreCase(key)) {
          return entry.getValue().asText();
        }
      }
    } else if (node.isArray()) {
      Iterator<JsonNode> arrayItemsIterator = node.elements();
      while (arrayItemsIterator.hasNext()) {
        value = recursivelyLookupJsonPayload(arrayItemsIterator.next(), key);
        if (value != null) {
          return value;
        }
      }
    }
    return value;
  }

  public static void extractObjectsByKey(JsonNode node, String searchKey,
      Collection<JsonNode> foundObjects) {

    if (node.isObject()) {
      Iterator<Map.Entry<String, JsonNode>> nodeIterator = node.fields();

      while (nodeIterator.hasNext()) {
        Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) nodeIterator.next();
        if (!entry.getValue().isValueNode()) {
          extractObjectsByKey(entry.getValue(), searchKey, foundObjects);
        }

        String name = entry.getKey();
        if (name.equalsIgnoreCase(searchKey)) {

          JsonNode entryValue = entry.getValue();

          if (entryValue.isArray()) {

            Iterator<JsonNode> arrayItemsIterator = entryValue.elements();
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

  public static void convertArrayIntoList(JsonNode node, Collection<JsonNode> instances) {

    if (node.isArray()) {
      Iterator<JsonNode> arrayItemsIterator = node.elements();
      while (arrayItemsIterator.hasNext()) {
        instances.add(arrayItemsIterator.next());
      }
    } else {
      instances.add(node);
    }
  }

  public static void extractFieldValuesFromObject(JsonNode node,
      Collection<String> attributesToExtract, Collection<String> fieldValues) {

    if (node.isObject()) {

      JsonNode valueNode = null;

      for (String attrToExtract : attributesToExtract) {

        valueNode = node.get(attrToExtract);

        if (valueNode != null) {

          if (valueNode.isValueNode()) {
            fieldValues.add(valueNode.asText());
          }
        }
      }
    }
  }


  public static String objToJson(Object obj) {
    JSONObject jsonObject = new JSONObject(obj);
    String json = jsonObject.toString();
    return json;
  }
}

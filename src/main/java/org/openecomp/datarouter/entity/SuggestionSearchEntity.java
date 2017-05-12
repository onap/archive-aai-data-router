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

import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openecomp.datarouter.util.NodeUtils;

import com.fasterxml.jackson.databind.JsonNode;

public class SuggestionSearchEntity implements DocumentStoreDataEntity, Serializable {
  private static final long serialVersionUID = -3636393943669310760L;

  protected String id; // generated SHA-256 digest
  private String entityType;
  private List<String> entityTypeAliases;
  private List<String> suggestionInputPermutations = new ArrayList<>();
  private List<String> statusPermutations = new ArrayList<>();
  private List<String> suggestableAttr = new ArrayList<>();
  private Map<String, String> payload = new HashMap<>();
  private JSONObject payloadJsonNode = new JSONObject();
  private StringBuffer outputString = new StringBuffer();

  public void deriveFields() throws NoSuchAlgorithmException {
    int payloadEntryCounter = 1;

    for (Map.Entry<String, String> payload : getPayload().entrySet()) {
      if (payload.getValue() != null && payload.getValue().length() > 0) {
        this.getPayloadJsonNode().put(payload.getKey(), payload.getValue());
        this.outputString.append(payload.getValue());

        if (payloadEntryCounter < getPayload().entrySet().size()) {
          this.outputString.append(" and ");
        } else {
          this.outputString.append(" ");
        }
      }

      payloadEntryCounter++;
    }

    this.outputString.append(getEntityTypeAliases().get(0));
    this.id = NodeUtils.generateUniqueShaDigest(outputString.toString());
  }

  /**
   * Launch pad for performing permutations of the entity type, aliases, prov status and orchestration status.
   * SHA-256 will result in an ID with a guaranteed uniqueness compared to just a java hashcode value.
   * @return
   */
  public List<String> generateSuggestionInputPermutations() {
    List<String> entityNames = new ArrayList<>();
    entityNames.add(entityType);

    if ((entityTypeAliases != null) && !(entityTypeAliases.isEmpty())) {
      for (String alias : entityTypeAliases) {
        entityNames.add(alias);
      }
    }

    ArrayList<String> listToPermutate = new ArrayList<>(statusPermutations);
    ArrayList<String> listOfSearchSuggestionPermutations = new ArrayList<>();

    for (String entityName : entityNames) {
      listToPermutate.add(entityName);
      permutateList(listToPermutate, new ArrayList<String>(), listToPermutate.size(), listOfSearchSuggestionPermutations);
      listToPermutate.remove(entityName);
    }

    return listOfSearchSuggestionPermutations;
  }

  public boolean isSuggestableDoc() {
    return this.getPayload().size() != 0;
  }
  
  /**
   * Generate all permutations of Entity Type and (Prov Status and/or Orchestration Status)
   * @param list The list of unique elements to create permutations of
   * @param permutation A list to hold the current permutation used during
   * @param size To keep track of the original size of the number of unique elements
   * @param listOfSearchSuggestionPermutationList The list to hold all of the different permutations
   */
  private void permutateList(List<String> list, List<String> permutation, int size,
      List<String> listOfSearchSuggestionPermutationList) {
    if (permutation.size() == size) {
      StringBuilder newPermutation = new StringBuilder();

      for (int i = 0; i < permutation.size(); i++) {
        newPermutation.append(permutation.get(i)).append(" ");
      }

      listOfSearchSuggestionPermutationList.add(newPermutation.toString().trim());

      return;
    }

    String[] availableItems = list.toArray(new String[0]);

    for (String i : availableItems) {
      permutation.add(i);
      list.remove(i);
      permutateList(list, permutation, size, listOfSearchSuggestionPermutationList);
      list.add(i);
      permutation.remove(i);
    }
  }

  /**
   * return Custom-built JSON representation of this class
   */
  @Override
  public String getAsJson() throws IOException {
    if (entityType == null || suggestionInputPermutations == null) {
      return null;
    }

    JSONObject rootNode = new JSONObject();
    JSONArray inputArray = new JSONArray();
    JSONObject payloadNode = new JSONObject();
    StringBuffer outputString = new StringBuffer();

    int payloadEntryCounter = 1;

    // Add prov and orchestration status to search suggestion string
    for (Map.Entry<String, String> payload : getPayload().entrySet()) {
      payloadNode.put(payload.getKey(), payload.getValue());
      outputString.append(payload.getValue());

      if (payloadEntryCounter < getPayload().entrySet().size()) {
        // Add the word "and" between prov and orchestration statuses, if both are present
        outputString.append(" and ");
        payloadEntryCounter++;
      }
    }

    // Add entity type to search suggestion string. We've decided to use the first entity type alias from the OXM
    outputString.append(" ").append(getEntityTypeAliases().get(0));

    for (String permutation : suggestionInputPermutations) {
      inputArray.put(permutation);
    }

    // Build up the search suggestion as JSON
    JSONObject entitySuggest = new JSONObject();
    entitySuggest.put("input", inputArray);
    entitySuggest.put("output", outputString);
    entitySuggest.put("payload", payloadNode);
    rootNode.put("entity_suggest", entitySuggest);

    return rootNode.toString();
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public List<String> getEntityTypeAliases() {
    return entityTypeAliases;
  }

  public void setEntityTypeAliases(List<String> entityTypeAliases) {
    this.entityTypeAliases = entityTypeAliases;
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public StringBuffer getOutputString() {
    return outputString;
  }

  public void setOutputString(StringBuffer outputString) {
    this.outputString = outputString;
  }

  public Map<String, String> getPayload() {
    return payload;
  }

  public void setPayloadFromResponse(JsonNode node) {
    Map<String, String> nodePayload = new HashMap<>();
    JsonNode entityNode = node.get("entity");
    if (suggestableAttr != null) {
      for (String attribute : suggestableAttr) {
        if (entityNode.get(attribute) != null && !entityNode.get(attribute).asText().trim().isEmpty()) {
          nodePayload.put(attribute, entityNode.get(attribute).asText());
          this.statusPermutations.add(entityNode.get(attribute).asText());
        }
      }
      this.setPayload(nodePayload);
    }
  }

  public void setPayload(Map<String, String> payload) {
    this.payload = payload;
  }

  public JSONObject getPayloadJsonNode() {
    return payloadJsonNode;
  }

  public void setPayloadJsonNode(JSONObject payloadJsonNode) {
    this.payloadJsonNode = payloadJsonNode;
  }

  public List<String> getStatusPermutations() {
    return statusPermutations;
  }

  public List<String> getSuggestableAttr() {
    return suggestableAttr;
  }

  public List<String> getSuggestionInputPermutations() {
    return this.suggestionInputPermutations;
  }

  public void setStatusPermutations(List<String> statusPermutations) {
    this.statusPermutations = statusPermutations;
  }

  public void setSuggestableAttr(ArrayList<String> attributes) {
    for (String attribute : attributes) {
      this.suggestableAttr.add(attribute);
    }
  }

  public void setSuggestionInputPermutations(List<String> permutations) {
    this.suggestionInputPermutations = permutations;
  }

  @Override
  public String toString() {
    return "SuggestionSearchEntity [id=" + id + ", entityType=" + entityType
        + ", entityTypeAliases=" + entityTypeAliases + ", suggestionInputPermutations="
        + suggestionInputPermutations + ", statusPermutations=" + statusPermutations
        + ", suggestableAttr=" + suggestableAttr + ", payload=" + payload + ", payloadJsonNode="
        + payloadJsonNode + ", outputString=" + outputString + "]";
  }
}

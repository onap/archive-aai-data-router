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
package org.onap.aai.datarouter.entity;

import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.aai.datarouter.search.filters.config.UiFilterConfig;
import org.onap.aai.datarouter.search.filters.config.UiFiltersConfig;
import org.onap.aai.datarouter.search.filters.config.UiFiltersSchemaUtility;
import org.onap.aai.datarouter.util.NodeUtils;
import org.onap.aai.datarouter.util.SearchSuggestionPermutation;

import com.fasterxml.jackson.databind.JsonNode;

public class SuggestionSearchEntity implements DocumentStoreDataEntity, Serializable {
  private static final long serialVersionUID = -3636393943669310760L;

  private static final String FILTER_ID = "filterId";
  private static final String FILTER_VALUE = "filterValue";
  private static final String FILTER_LIST = "filterList";

  protected String id; // generated SHA-256 digest
  private String entityType;
  private List<String> entityTypeAliases;
  private List<String> suggestionInputPermutations = new ArrayList<>();
  private List<String> statusPermutations = new ArrayList<>();
  private List<String> suggestableAttr = new ArrayList<>();
  
  private Map<String, String> inputOutputData = new HashMap<>();
  private Map<String, UiFilterConfig> filters = new HashMap<>();
  private JSONObject filterPayload = new JSONObject();
  private StringBuilder searchSuggestionDisplayString = new StringBuilder();
  private JSONArray payloadFilters = new JSONArray();
  private UiFiltersSchemaUtility filtersSchemaUtility = new UiFiltersSchemaUtility();

  public SuggestionSearchEntity() {
    UiFiltersConfig filterConfigList = filtersSchemaUtility.loadUiFiltersConfig();

    // Populate the map with keys that will match the suggestableAttr values
    for (UiFilterConfig filter : filterConfigList.getFilters()) {
      if (filter.getDataSource() != null) {
        filters.put(filter.getDataSource().getFieldName(), filter);
      }
    }
  }

  /**
   * Create the search suggestion string to display to the user in the search suggestion drop-down
   * 
   * @throws NoSuchAlgorithmException
   */
  public void generateSearchSuggestionDisplayStringAndId() throws NoSuchAlgorithmException {
    int payloadEntryCounter = 1;

    for (Map.Entry<String, String> outputValue : inputOutputData.entrySet()) {
      if (outputValue.getValue() != null && outputValue.getValue().length() > 0) {
        this.searchSuggestionDisplayString.append(outputValue.getValue());

        if (payloadEntryCounter < inputOutputData.entrySet().size()) {
          this.searchSuggestionDisplayString.append(" and ");
        } else {
          this.searchSuggestionDisplayString.append(" ");
        }
      }

      payloadEntryCounter++;
    }

    this.searchSuggestionDisplayString.append(getEntityTypeAliases().get(0));
    generateSearchSuggestionId(searchSuggestionDisplayString.toString());
  }
  
  /**
   * Generates an ID by encrypting the string to display to the user in the search suggestion
   * drop-down
   * 
   * @param outputString The string to create the encrypted ID from
   */
  private void generateSearchSuggestionId(String searchSuggestionDisplayString) {
    this.id = NodeUtils.generateUniqueShaDigest(searchSuggestionDisplayString);
  }

  /**
   * Launch pad for performing permutations of the entity type, aliases, prov status and orchestration status.
   * SHA-256 will result in an ID with a guaranteed uniqueness compared to just a java hashcode value
   * 
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

    ArrayList<String> listOfSearchSuggestionPermutations = new ArrayList<>();
    ArrayList<String> listToPermutate = 
        new ArrayList<>(this.getInputOutputData().values());

    for (String entityName : entityNames) {
      listToPermutate.add(entityName);
      List<List<String>> lists = SearchSuggestionPermutation.getListPermutations(listToPermutate);
      for (List<String> li : lists){
        listOfSearchSuggestionPermutations.add(String.join(" ", li));
      }
      listToPermutate.remove(entityName);
    }

    return listOfSearchSuggestionPermutations;
  }

  /**
   * Return a custom JSON representation of this class
   */
  @Override
  public String getAsJson() throws IOException {
    if (entityType == null || suggestionInputPermutations == null) {
      return null;
    }

    JSONObject rootNode = new JSONObject();
    JSONArray inputArray = new JSONArray();
    JSONObject payloadNode = new JSONObject();
    StringBuilder outputString = new StringBuilder();

    int payloadEntryCounter = 1;

    // Add prov and orchestration status to search suggestion string
    for (Map.Entry<String, String> payload : inputOutputData.entrySet()) {
      payloadNode.put(payload.getKey(), payload.getValue());
      outputString.append(payload.getValue());

      if (payloadEntryCounter < inputOutputData.entrySet().size()) {
        // Add the word "and" between prov and orchestration statuses, if both are present
        outputString.append(" and ");
        payloadEntryCounter++;
      }
    }

    /* Add entity type to search suggestion string. We've decided to use the first entity type alias
     * from the OXM */
    outputString.append(" ").append(getEntityTypeAliases().get(0));

    for (String permutation : suggestionInputPermutations) {
      inputArray.put(permutation);
    }

    // Build up the search suggestion as JSON
    JSONObject entitySuggest = new JSONObject();
    entitySuggest.put("input", inputArray);
    entitySuggest.put("output", outputString);
    entitySuggest.put("payload", this.filterPayload);
    rootNode.put("entity_suggest", entitySuggest);

    return rootNode.toString();
  }

  public boolean isSuggestableDoc() {
    return this.getFilterPayload().length() != 0;
  }

  /**
   * Generate all permutations of Entity Type and (Prov Status and/or Orchestration Status)
   * 
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
   * Populate a string that will represent the UI filters portion of the JSON payload that's stored in the
   * search engine
   * 
   * @param entityFromUebEvent
   * @param suggestibleAttrInPayload
   */
  public void setFilterBasedPayloadFromResponse(JsonNode entityFromUebEvent,
      List<String> suggestibleAttrInOxm, List<String> suggestibleAttrInPayload) {
    if (suggestibleAttrInOxm != null) {
      for (String attribute : suggestibleAttrInOxm) {
        UiFilterConfig filterConfig = filters.get(attribute);

        if (suggestibleAttrInPayload.contains(attribute)) {
          inputOutputData.put(attribute, entityFromUebEvent.get(attribute).asText());

          if(filterConfig != null) {
            JSONObject jsonFilterPayload = new JSONObject();
            jsonFilterPayload.put(FILTER_ID, filterConfig.getFilterId());
            jsonFilterPayload.put(FILTER_VALUE, entityFromUebEvent.get(attribute).asText());
            this.payloadFilters.put(jsonFilterPayload);
          } else {
            this.filterPayload.put(attribute, entityFromUebEvent.get(attribute).asText()); 
          }
        } else {
          if(filterConfig != null) {
            JSONObject emptyValueFilterPayload = new JSONObject();
            emptyValueFilterPayload.put(FILTER_ID, filterConfig.getFilterId());
            this.payloadFilters.put(emptyValueFilterPayload);
          }
        }
      }

      this.filterPayload.put(FILTER_LIST, this.payloadFilters);
    }
  }

  public void setPayloadFromResponse(JsonNode node) {
    if (suggestableAttr != null) {
      for (String attribute : suggestableAttr) {
        if (node.get(attribute) != null) {
          inputOutputData.put(attribute, node.get(attribute).asText());
          this.filterPayload.put(attribute, node.get(attribute).asText());
        }
      }
    }
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

  public StringBuilder getSearchSuggestionDisplayString() {
    return searchSuggestionDisplayString;
  }

  public JSONObject getFilterPayload() {
    return filterPayload;
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

  public void setId(String id) {
    this.id = id;
  }
  
  public void setInputOutputData(Map<String, String> inputOutputData) {
    this.inputOutputData = inputOutputData;
  }

  public Map<String, String> getInputOutputData() {
    return inputOutputData;
  }

  public void setSearchSuggestionDisplayString(StringBuilder searchSuggestionDisplayString) {
    this.searchSuggestionDisplayString = searchSuggestionDisplayString;
  }

  public void setFilterPayload(JSONObject filterPayload) {
    this.filterPayload = filterPayload;
  }
  
  public void setFiltersSchemaUtility(UiFiltersSchemaUtility filtersSchemaUtility) {
    this.filtersSchemaUtility = filtersSchemaUtility;
  }

  public void setStatusPermutations(List<String> statusPermutations) {
    this.statusPermutations = statusPermutations;
  }

  public void setSuggestableAttr(List<String> attributes) {
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
        + ", suggestableAttr=" + suggestableAttr + ", inputOutputData=" + inputOutputData
        + ", filters=" + filters + ", filterPayload=" + filterPayload
        + ", searchSuggestionDisplayString=" + searchSuggestionDisplayString + ", payloadFilters="
        + payloadFilters + "]";
  }
}

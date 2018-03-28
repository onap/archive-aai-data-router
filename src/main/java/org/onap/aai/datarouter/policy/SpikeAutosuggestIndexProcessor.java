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
package org.onap.aai.datarouter.policy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.aai.datarouter.entity.OxmEntityDescriptor;
import org.onap.aai.datarouter.entity.SpikeEventVertex;
import org.onap.aai.datarouter.entity.SuggestionSearchEntity;
import org.onap.aai.datarouter.logging.EntityEventPolicyMsgs;
import org.onap.aai.datarouter.util.EntityOxmReferenceHelper;
import org.onap.aai.datarouter.util.SearchSuggestionPermutation;
import org.onap.aai.datarouter.util.Version;
import org.onap.aai.datarouter.util.VersionedOxmEntities;

import com.fasterxml.jackson.databind.JsonNode;


public class SpikeAutosuggestIndexProcessor extends AbstractSpikeEntityEventProcessor {

  public static final String additionalInfo = "Response of SpikeEntityEventPolicy";

  private final String EVENT_VERTEX = "vertex";

  private String oxmVersion = null;

  /** Agent for communicating with the Search Service. */

  public SpikeAutosuggestIndexProcessor(SpikeEventPolicyConfig config)
      throws FileNotFoundException {
    super(config);
    parseLatestOxmVersion();
  }

  @Override
  protected void startup() {
    // Create the indexes in the search service if they do not already exist.
    searchAgent.createSearchIndex(searchIndexName, searchIndexSchema, createIndexUrl);
    logger.info(EntityEventPolicyMsgs.ENTITY_EVENT_POLICY_REGISTERED);
  }

  @Override
  public void process(Exchange exchange) throws Exception {
   
    long startTime = System.currentTimeMillis();
    String uebPayload = getExchangeBody(exchange);
    if (uebPayload == null) {
      return;
    }
    JsonNode uebAsJson = null;
    try {
      uebAsJson = mapper.readTree(uebPayload);
    } catch (IOException e) {
      returnWithError(exchange, uebPayload, "Invalid Payload");
      return;
    }
    
    String action = getSpikeEventAction(exchange, uebPayload);
    if (action == null) {
      return;
    }
    JSONObject uebObjEntity = getUebContentAsJson(uebPayload, EVENT_VERTEX);
    if (uebObjEntity == null) {
      returnWithError(exchange, uebPayload, "Payload is missing " + EVENT_VERTEX);
      return;
    }
    
    SpikeEventVertex eventVertex = populateEventVertex(exchange, uebPayload);
    if (eventVertex == null) {
      return;
    }
    String entityType = getEntityType(exchange, eventVertex, uebPayload);
    if (entityType == null) {
      return;
    }
    String entityLink = getEntityLink(exchange, eventVertex, uebPayload);
    if (entityLink == null) {
      return;
    }
    DynamicJAXBContext oxmJaxbContext = readOxm(exchange, uebPayload);
    if (oxmJaxbContext == null) {
      return;
    }
    String oxmEntityType = getOxmEntityType(entityType);
    List<String> searchableAttr =  getSearchableAttibutes(oxmJaxbContext, oxmEntityType, entityType, uebPayload,
        exchange);
    if (searchableAttr == null) {
      return;
    }    
   
    // log the fact that all data are in good shape
    logger.info(EntityEventPolicyMsgs.PROCESS_ENTITY_EVENT_POLICY_NONVERBOSE, action, entityType);
    logger.debug(EntityEventPolicyMsgs.PROCESS_ENTITY_EVENT_POLICY_VERBOSE, action, entityType,
        uebPayload);
    
        
    /*
     * Use the versioned OXM Entity class to get access to cross-entity reference helper collections
     */
    VersionedOxmEntities oxmEntities =
        EntityOxmReferenceHelper.getInstance().getVersionedOxmEntities(Version.valueOf(oxmVersion));
    
    /*
     * Process for autosuggestable entities
     */
    if (oxmEntities != null) {
      Map<String, OxmEntityDescriptor> rootDescriptor =
          oxmEntities.getSuggestableEntityDescriptors();
      if (!rootDescriptor.isEmpty()) {
        List<String> suggestibleAttrInPayload = new ArrayList<>();
        List<String> suggestibleAttrInOxm = extractSuggestableAttr(oxmEntities, entityType);
        if (suggestibleAttrInOxm != null) {
          for (String attr: suggestibleAttrInOxm){
            if ( uebAsJson.get("vertex").get("properties").has(attr) ){
              suggestibleAttrInPayload.add(attr);
            }
          }
        }

        if (suggestibleAttrInPayload.isEmpty()) {
          return;
        }
        List<String> suggestionAliases = extractAliasForSuggestableEntity(oxmEntities, entityType);       

        /*
         * It was decided to silently ignore DELETE requests for resources we don't allow to be
         * deleted. e.g. auto-suggestion deletion is not allowed while aggregation deletion is.
         */
        if (!ACTION_DELETE.equalsIgnoreCase(action)) {
          List<ArrayList<String>> listOfValidPowerSetElements =
              SearchSuggestionPermutation.getNonEmptyUniqueLists(suggestibleAttrInPayload);

          // Now we have a list containing the power-set (minus empty element) for the status that are
          // available in the payload. Try inserting a document for every combination.
          for (ArrayList<String> list : listOfValidPowerSetElements) {
            SuggestionSearchEntity suggestionSearchEntity = new SuggestionSearchEntity();
            suggestionSearchEntity.setEntityType(entityType);
            suggestionSearchEntity.setSuggestableAttr(list);
            suggestionSearchEntity.setEntityTypeAliases(suggestionAliases);
            suggestionSearchEntity.setFilterBasedPayloadFromResponse(uebAsJson.get("vertex").get("properties"),
                suggestibleAttrInOxm, list);
            suggestionSearchEntity.setSuggestionInputPermutations(
                suggestionSearchEntity.generateSuggestionInputPermutations());

            if (suggestionSearchEntity.isSuggestableDoc()) {
              try {
                suggestionSearchEntity.generateSearchSuggestionDisplayStringAndId();
              } catch (NoSuchAlgorithmException e) {
                logger.error(EntityEventPolicyMsgs.DISCARD_UPDATING_SEARCH_SUGGESTION_DATA,
                    "Cannot create unique SHA digest for search suggestion data. Exception: "
                        + e.getLocalizedMessage());
              }

              handleSearchServiceOperation(suggestionSearchEntity, action, searchIndexName);
            }
          }
        }
      }
    }
    long stopTime = System.currentTimeMillis();
    metricsLogger.info(EntityEventPolicyMsgs.OPERATION_RESULT_NO_ERRORS, PROCESS_SPIKE_EVENT,
        String.valueOf(stopTime - startTime));
    setResponse(exchange, ResponseType.SUCCESS, additionalInfo);
    return;
  }

  public List<String> extractSuggestableAttr(VersionedOxmEntities oxmEntities, String entityType) {
    // Extract suggestable attributeshandleTopographicalData
    Map<String, OxmEntityDescriptor> rootDescriptor = oxmEntities.getSuggestableEntityDescriptors();

    if (rootDescriptor == null) {
      return Collections.emptyList();
    }

    OxmEntityDescriptor desc = rootDescriptor.get(entityType);

    if (desc == null) {
      return Collections.emptyList();
    }

    return desc.getSuggestableAttributes();
  }


  public List<String> extractAliasForSuggestableEntity(VersionedOxmEntities oxmEntities,
      String entityType) {

    // Extract alias
    Map<String, OxmEntityDescriptor> rootDescriptor = oxmEntities.getEntityAliasDescriptors();

    if (rootDescriptor == null) {
      return Collections.emptyList();
    }

    OxmEntityDescriptor desc = rootDescriptor.get(entityType);
    return desc.getAlias();
  }

  private void parseLatestOxmVersion() {
    int latestVersion = -1;
    if (oxmVersionContextMap != null) {
      Iterator it = oxmVersionContextMap.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry pair = (Map.Entry) it.next();

        String version = pair.getKey().toString();
        int versionNum = Integer.parseInt(version.substring(1, version.length()));

        if (versionNum > latestVersion) {
          latestVersion = versionNum;
          oxmVersion = pair.getKey().toString();
        }

        logger.info(EntityEventPolicyMsgs.PROCESS_OXM_MODEL_FOUND, pair.getKey().toString());
      }
    } else {
      logger.error(EntityEventPolicyMsgs.PROCESS_OXM_MODEL_MISSING, "");
    }
  }
  
  

}

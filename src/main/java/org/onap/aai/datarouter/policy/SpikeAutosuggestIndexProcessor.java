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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.onap.aai.datarouter.entity.SpikeEventMeta;
import org.onap.aai.datarouter.entity.SuggestionSearchEntity;
import org.onap.aai.datarouter.logging.EntityEventPolicyMsgs;
import org.onap.aai.datarouter.util.SearchSuggestionPermutation;
import org.onap.aai.entity.OxmEntityDescriptor;
import org.onap.aai.util.EntityOxmReferenceHelper;
import org.onap.aai.util.Version;
import org.onap.aai.util.VersionedOxmEntities;

import com.fasterxml.jackson.databind.JsonNode;

public class SpikeAutosuggestIndexProcessor extends AbstractSpikeEntityEventProcessor {

  public static final String additionalInfo = "Response of SpikeEntityEventPolicy";

  private static final String PROCESS_SPIKE_EVENT = "Process Spike Event";

  
  /** Agent for communicating with the Search Service. */

  public SpikeAutosuggestIndexProcessor(SpikeEventPolicyConfig config)
      throws FileNotFoundException {
    super(config);
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
    
    SpikeEventMeta meta = processSpikeEvent(exchange);
    
    if ( meta == null ) {
      return;
    }
 
    /*
     * Use the versioned OXM Entity class to get access to cross-entity reference helper collections
     */
    VersionedOxmEntities oxmEntities =
        EntityOxmReferenceHelper.getInstance().getVersionedOxmEntities(Version.valueOf(oxmVersion.toLowerCase()));
    
    if (oxmEntities != null) {
      Map<String, OxmEntityDescriptor> rootDescriptor =
          oxmEntities.getSuggestableEntityDescriptors();
      if (!rootDescriptor.isEmpty()) {
        List<String> suggestibleAttrInPayload = new ArrayList<>();
        List<String> suggestibleAttrInOxm =
            extractSuggestableAttr(oxmEntities, meta.getSpikeEventVertex().getType());
        if (suggestibleAttrInOxm != null) {
          for (String attr : suggestibleAttrInOxm) {
            if (meta.getVertexProperties().has(attr)) {
              suggestibleAttrInPayload.add(attr);
            }
          }
        }

        if (suggestibleAttrInPayload.isEmpty()) {
          return;
        }
        List<String> suggestionAliases = extractAliasForSuggestableEntity(oxmEntities,  meta.getSpikeEventVertex().getType());       

        /*
         * It was decided to silently ignore DELETE requests for resources we don't allow to be
         * deleted. e.g. auto-suggestion deletion is not allowed while aggregation deletion is.
         */
        if (!DELETE.equalsIgnoreCase(meta.getBodyOperationType())) {
          List<ArrayList<String>> listOfValidPowerSetElements =
              SearchSuggestionPermutation.getNonEmptyUniqueLists(suggestibleAttrInPayload);
          
          JsonNode propertiesNode = mapper.readValue(meta.getVertexProperties().toString(), JsonNode.class);
          
          // Now we have a list containing the power-set (minus empty element) for the status that are
          // available in the payload. Try inserting a document for every combination.
          for (ArrayList<String> list : listOfValidPowerSetElements) {
            SuggestionSearchEntity suggestionSearchEntity = new SuggestionSearchEntity();
            suggestionSearchEntity.setEntityType(meta.getSpikeEventVertex().getType());
            suggestionSearchEntity.setSuggestableAttr(list);
            suggestionSearchEntity.setEntityTypeAliases(suggestionAliases);
            suggestionSearchEntity.setFilterBasedPayloadFromResponse(propertiesNode,
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

              handleSearchServiceOperation(suggestionSearchEntity, meta.getBodyOperationType(), searchIndexName);
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

}

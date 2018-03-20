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
package org.onap.aai.datarouter.policy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.camel.Exchange;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.onap.aai.datarouter.entity.SpikeAggregationEntity;
import org.onap.aai.datarouter.entity.SpikeEventVertex;
import org.onap.aai.datarouter.logging.EntityEventPolicyMsgs;

import com.fasterxml.jackson.databind.JsonNode;


public class SpikeAggregateGenericVnfProcessor extends AbstractSpikeEntityEventProcessor {

  public static final String additionalInfo = "Response of SpikeEntityEventPolicy";

  /** Agent for communicating with the Search Service. */

  public SpikeAggregateGenericVnfProcessor(SpikeEventPolicyConfig config)
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

    SpikeAggregationEntity spikeAgregationEntity = new SpikeAggregationEntity();
    spikeAgregationEntity.setLink(entityLink);
    spikeAgregationEntity.deriveFields(uebAsJson);
    handleSearchServiceOperation(spikeAgregationEntity, action, searchIndexName);

    long stopTime = System.currentTimeMillis();
    metricsLogger.info(EntityEventPolicyMsgs.OPERATION_RESULT_NO_ERRORS, PROCESS_SPIKE_EVENT,
        String.valueOf(stopTime - startTime));
    setResponse(exchange, ResponseType.SUCCESS, additionalInfo);
    return;
  }

}

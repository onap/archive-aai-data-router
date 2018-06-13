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
import java.util.List;

import org.apache.camel.Exchange;
import org.onap.aai.datarouter.entity.SpikeAggregationEntity;
import org.onap.aai.datarouter.entity.SpikeEventMeta;
import org.onap.aai.datarouter.logging.EntityEventPolicyMsgs;

import com.fasterxml.jackson.databind.JsonNode;


public class SpikeAggregateGenericVnfProcessor extends AbstractSpikeEntityEventProcessor {

  public static final String additionalInfo = "Response of SpikeEntityEventPolicy";

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

    SpikeEventMeta meta = processSpikeEvent(exchange);

    if (meta == null) {
      return;
    }

    String oxmEntityType = getOxmEntityType(meta.getSpikeEventVertex().getType());

    List<String> searchableAttr = getSearchableAttibutes(meta.getOxmJaxbContext(), oxmEntityType,
        meta.getSpikeEventVertex().getType(), meta.getEventEntity().toString(), exchange);

    if (searchableAttr == null) {
      return;
    }

    JsonNode propertiesNode =
        mapper.readValue(meta.getVertexProperties().toString(), JsonNode.class);

    SpikeAggregationEntity spikeAgregationEntity = new SpikeAggregationEntity();
    spikeAgregationEntity.setLink(meta.getSpikeEventVertex().getEntityLink());
    spikeAgregationEntity.deriveFields(propertiesNode);

    handleSearchServiceOperation(spikeAgregationEntity, meta.getBodyOperationType(),
        searchIndexName);

    long stopTime = System.currentTimeMillis();
    metricsLogger.info(EntityEventPolicyMsgs.OPERATION_RESULT_NO_ERRORS, PROCESS_SPIKE_EVENT,
        String.valueOf(stopTime - startTime));
    setResponse(exchange, ResponseType.SUCCESS, additionalInfo);
    return;
  }

}

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

import org.apache.camel.Exchange;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.datarouter.logging.EntityEventPolicyMsgs;



public class GapEventTransformer {
  protected Logger logger;
  protected static final String additionalInfo = "Response of GapEvent to transform";

  protected enum ResponseType {
    SUCCESS, PARTIAL_SUCCESS, FAILURE;
  }

  public GapEventTransformer() throws FileNotFoundException {
    LoggerFactory loggerFactoryInstance = LoggerFactory.getInstance();
    logger = loggerFactoryInstance.getLogger(AbstractSpikeEntityEventProcessor.class.getName());
  }


  public void process(Exchange exchange) {

    String payload = getExchangeBody(exchange);
    JSONObject newPayload = transformToSpikePattern(payload);
    exchange.getOut().setBody(newPayload.toString());

  }

  protected JSONObject transformToSpikePattern(String payload) {
    JSONObject payloadJson = new JSONObject(payload);
    JSONObject payloadEntity = payloadJson.getJSONObject("entity");
    JSONObject payloadProperties = payloadEntity.getJSONObject("properties");
    String payloadId = payloadEntity.getString("id");
    String payloadType = payloadEntity.getString("type");

    JSONObject newPayload = new JSONObject();
    newPayload.put("operation", "UPDATE");
    JSONObject newPayloadVertex = new JSONObject();
    newPayloadVertex.put("key", payloadId);
    newPayloadVertex.put("type", payloadType);
    newPayloadVertex.put("properties", payloadProperties);
    newPayload.put("vertex", newPayloadVertex);
    return newPayload;
  }

  protected String getExchangeBody(Exchange exchange) {
    String uebPayload = exchange.getIn().getBody().toString();
    if (uebPayload == null || !isJSONValid(uebPayload)) {
      uebPayload = exchange.getIn().getBody(String.class);
      if (uebPayload == null || !isJSONValid(uebPayload)) {
        returnWithError(exchange, uebPayload, "Invalid Payload");
        return null;
      }
    }
    return uebPayload;
  }

  private boolean isJSONValid(String test) {
    try {
      new JSONObject(test);
    } catch (JSONException ex) {
      return false;
    }
    return true;
  }

  protected void returnWithError(Exchange exchange, String payload, String errorMsg) {
    logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE, errorMsg);
    logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE, errorMsg, payload);
    setResponse(exchange, ResponseType.FAILURE, additionalInfo);
  }

  protected void setResponse(Exchange exchange, ResponseType responseType, String additionalInfo) {

    exchange.getOut().setHeader("ResponseType", responseType.toString());
    exchange.getOut().setBody(additionalInfo);
  }
}

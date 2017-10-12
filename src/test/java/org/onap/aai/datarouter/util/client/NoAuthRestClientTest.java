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

package org.onap.aai.datarouter.util.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.ws.rs.core.MediaType;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.aai.datarouter.util.client.NoAuthRestClient;
import org.onap.aai.restclient.client.OperationResult;
import org.onap.aai.restclient.client.RestClient;

public class NoAuthRestClientTest {

  RestClient client = null;
  OperationResult successResult = null;
  OperationResult failureResult = null;
  Exchange exchange = null;
  NoAuthRestClient narc = new NoAuthRestClient(60,60);
  String goodDomain = "AGoodUrlThatNeverFails.com";
  String badDomain = "ABadUrlThatAlwaysFails.com";
  String goodTargetUrl = "http://" + goodDomain + ":1234/servicegraph";
  String badTargetUrl = "http://" + badDomain + ":1234/servicegraph";
  String payload = "{\"origin-uri\":\"/routerService/servicegraph\","
      + "\"origin-payload\":{\"hashId\":\"claymore-sdwan-service.full.(View and Inspect)\"}}";
  
  String successResponsePayload = "very-good-result";
  String failureResponsePayload = "Server Error";
  
  @SuppressWarnings("unchecked")
  @Before
  public void init(){
    client = Mockito.mock(RestClient.class);
    successResult = new OperationResult(200, successResponsePayload);
    failureResult = new OperationResult(500, failureResponsePayload);
    failureResult.setFailureCause(failureResponsePayload);
    Mockito.when(client.post(Mockito.eq(goodTargetUrl), Mockito.anyString(), Mockito.anyMap(), 
        Mockito.eq(MediaType.APPLICATION_JSON_TYPE), Mockito.eq(MediaType.APPLICATION_JSON_TYPE)))
    .thenReturn(successResult);
    Mockito.when(client.post(Mockito.eq(badTargetUrl), Mockito.anyString(), Mockito.anyMap(), 
        Mockito.eq(MediaType.APPLICATION_JSON_TYPE), Mockito.eq(MediaType.APPLICATION_JSON_TYPE)))
    .thenReturn(failureResult);
    narc.setRestClient(client);

  }
  
  public Exchange getExchange(){
    CamelContext ctx = new DefaultCamelContext(); 
    Exchange ex = new DefaultExchange(ctx);
    ex.getIn().setHeader(Exchange.HTTP_URL, "http://ARandomOrigin.com");
    ex.getIn().setBody(payload);
    return ex;
  }
  
  @Test
  public void testHandleRequest_successScenario() {
    Exchange ex = getExchange();
    try {
      narc.handleRequest(goodDomain, "1234", ex);
      String outBody = ex.getOut().getBody(String.class);
      assertEquals("Routing success scenario: Failure to get correct http status.", 
          ex.getOut().getHeader(Exchange.HTTP_RESPONSE_CODE), 200 );
      assertEquals("Routing success scenario: Failure to get response body.", 
          outBody, successResponsePayload);
    } catch (Exception e) {
      fail("Routing success scenario: Failure to process.");
    }
  }
  
  @Test
  public void testHandleRequest_failureScenario() {
    Exchange ex = getExchange();
    try {
      narc.handleRequest(badDomain, "1234", ex);
      String outBody = ex.getOut().getBody(String.class);
      assertEquals("Routing failure scenario: Failure to get correct http status.", 
          ex.getOut().getHeader(Exchange.HTTP_RESPONSE_CODE), 500 );
      assertEquals("Routing failure scenario: Failure to get response body.", 
          outBody, failureResult.getFailureCause());
    } catch (Exception e) {
      fail("Routing failure scenario: Failure to process.");
    }
  }

}

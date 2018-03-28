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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;

import java.io.File;
import java.io.FileInputStream;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.onap.aai.datarouter.util.NodeUtils;
import org.onap.aai.datarouter.util.SearchServiceAgent;
import org.powermock.api.mockito.PowerMockito;



public class SpikeAutosuggestProcessorTest {
  SpikeAutosuggestIndexProcessor policy;
  String eventJson;

  @SuppressWarnings("unchecked")
  @Before
  public void init() throws Exception {
    SpikeEventPolicyConfig config = PowerMockito.mock(SpikeEventPolicyConfig.class);
    PowerMockito.when(config.getSearchKeystorePwd()).thenReturn("password");
    PowerMockito.when(config.getSourceDomain()).thenReturn("JUNIT");


    SearchServiceAgent searchServiceAgent = PowerMockito.mock(SearchServiceAgent.class);
    PowerMockito.whenNew(SearchServiceAgent.class).withAnyArguments()
        .thenReturn(searchServiceAgent);


    policy = new SpikeAutosuggestProcessorStubbed(config);
    FileInputStream event = new FileInputStream(new File("src/test/resources/spike_event.json"));
    eventJson = IOUtils.toString(event, "UTF-8");  

  }

  @Test
  public void testProcess_success() throws Exception {
    policy.process(getExchangeEvent("77777", "create", "generic-vnf"));
    
    assertNotNull(
        InMemorySearchDatastore.get(NodeUtils.generateUniqueShaDigest("junk and Running VNFs")));
    assertNotNull(
        InMemorySearchDatastore.get(NodeUtils.generateUniqueShaDigest("junk VNFs")));
    assertNotNull(
        InMemorySearchDatastore.get(NodeUtils.generateUniqueShaDigest("Running VNFs")));
   
   
  }
  @Test
  public void testProcess_fail() throws Exception {
    policy.process(getExchangeEvent("666666", "create", "NotValid"));
    assertNull(
        InMemorySearchDatastore.get(NodeUtils.generateUniqueShaDigest("NotValid/666666")));
    
    policy.process(getExchangeEvent("", "create", "generic-vnf"));
    assertNull(
        InMemorySearchDatastore.get(NodeUtils.generateUniqueShaDigest("generic-vnf/")));

  }

  
  private Exchange getExchangeEvent(String key, String action, String type) {
    Object obj = eventJson.replace("$KEY", key).replace("$ACTION", action).replace("$TYPE", type);
    Exchange exchange = PowerMockito.mock(Exchange.class);
    Message inMessage = PowerMockito.mock(Message.class);
    Message outMessage = PowerMockito.mock(Message.class);
    PowerMockito.when(exchange.getIn()).thenReturn(inMessage); 
    PowerMockito.when(inMessage.getBody()).thenReturn(obj);

    PowerMockito.when(exchange.getOut()).thenReturn(outMessage);
    PowerMockito.doNothing().when(outMessage).setBody(anyObject());
    PowerMockito.doNothing().when(outMessage).setHeader(anyString(), anyObject());

    return exchange;

  }



}

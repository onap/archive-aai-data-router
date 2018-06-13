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

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;

import java.io.File;
import java.io.FileInputStream;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.onap.aai.datarouter.policy.EntityEventPolicy;
import org.onap.aai.datarouter.policy.EntityEventPolicyConfig;
import org.onap.aai.datarouter.util.NodeUtils;
import org.onap.aai.datarouter.util.SearchServiceAgent;
import org.powermock.api.mockito.PowerMockito;



public class EntityEventPolicyTest {
	private EntityEventPolicy policy;
	private String eventJson;
	private InMemorySearchDatastore searchDb;
	
	@SuppressWarnings("unchecked")
    @Before
    public void init() throws Exception {
		EntityEventPolicyConfig config = PowerMockito.mock(EntityEventPolicyConfig.class); 
		PowerMockito.when(config.getSearchKeystorePwd()).thenReturn("password");
		PowerMockito.when(config.getSourceDomain()).thenReturn("JUNIT");
		
		searchDb = new InMemorySearchDatastore();
		policy = new EntityEventPolicyStubbed(config).withSearchDb(searchDb);
		
		FileInputStream event = new FileInputStream( new File("src/test/resources/aai_event.json"));
		eventJson = IOUtils.toString(event, "UTF-8");

	}

	@Test
	 public void testProcess() throws Exception {
		policy.process(getExchangeEvent("event1","create"));
		policy.process(getExchangeEvent("event2","create"));
		
		assertNotNull(searchDb.get(NodeUtils.generateUniqueShaDigest("event1")));
		assertNotNull(searchDb.get(NodeUtils.generateUniqueShaDigest("event2")));
		
		policy.process(getExchangeEvent("event1","update"));
		policy.process(getExchangeEvent("event2","update"));
		assertNotNull(searchDb.get(NodeUtils.generateUniqueShaDigest("event1")));
		assertNotNull(searchDb.get(NodeUtils.generateUniqueShaDigest("event2")));
		
		policy.process(getExchangeEvent("event2","delete"));
		assertNull(searchDb.get(NodeUtils.generateUniqueShaDigest("event2")));
	}
	
	private Exchange getExchangeEvent(String link,String action){
		Object obj = eventJson.replace("$LINK",link ).replace("$ACTION",action) ;
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

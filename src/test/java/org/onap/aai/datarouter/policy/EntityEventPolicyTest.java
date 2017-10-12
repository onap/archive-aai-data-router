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
	EntityEventPolicy policy;
	String eventJson;
	
	@SuppressWarnings("unchecked")
    @Before
    public void init() throws Exception {
		EntityEventPolicyConfig config = PowerMockito.mock(EntityEventPolicyConfig.class); 
		PowerMockito.when(config.getSearchKeystorePwd()).thenReturn("password");
		PowerMockito.when(config.getSourceDomain()).thenReturn("JUNIT");
		
		
		SearchServiceAgent searchServiceAgent = PowerMockito.mock(SearchServiceAgent.class); 
		
		PowerMockito.whenNew(SearchServiceAgent.class).withAnyArguments().thenReturn(searchServiceAgent);
		
		
		policy = new EntityEventPolicyStubbed(config);
		FileInputStream event = new FileInputStream( new File("src/test/resources/aai_event.json"));
		eventJson = IOUtils.toString(event, "UTF-8");

	}

	@Test
	 public void testProcess() throws Exception {
		policy.process(getExchangeEvent("event1","create"));
		policy.process(getExchangeEvent("event2","create"));
		
		assertNotNull(InMemorySearchDatastore.get(NodeUtils.generateUniqueShaDigest("event1")));
		assertNotNull(InMemorySearchDatastore.get(NodeUtils.generateUniqueShaDigest("event2")));
		
		policy.process(getExchangeEvent("event1","update"));
		policy.process(getExchangeEvent("event2","update"));
		assertNotNull(InMemorySearchDatastore.get(NodeUtils.generateUniqueShaDigest("event1")));
		assertNotNull(InMemorySearchDatastore.get(NodeUtils.generateUniqueShaDigest("event2")));
		
		policy.process(getExchangeEvent("event2","delete"));
		assertNull(InMemorySearchDatastore.get(NodeUtils.generateUniqueShaDigest("event2")));
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

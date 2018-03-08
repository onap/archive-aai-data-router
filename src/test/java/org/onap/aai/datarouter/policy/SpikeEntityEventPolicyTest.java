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



public class SpikeEntityEventPolicyTest {
  SpikeEntityEventPolicy policy;
  String eventJson;

  @SuppressWarnings("unchecked")
  @Before
  public void init() throws Exception {
    SpikeEntityEventPolicyConfig config = PowerMockito.mock(SpikeEntityEventPolicyConfig.class);
    PowerMockito.when(config.getSearchKeystorePwd()).thenReturn("password");
    PowerMockito.when(config.getSourceDomain()).thenReturn("JUNIT");


    SearchServiceAgent searchServiceAgent = PowerMockito.mock(SearchServiceAgent.class);
    PowerMockito.whenNew(SearchServiceAgent.class).withAnyArguments()
        .thenReturn(searchServiceAgent);


    policy = new SpikeEntityEventPolicyStubbed(config);
    FileInputStream event = new FileInputStream(new File("src/test/resources/spike_event.json"));
    eventJson = IOUtils.toString(event, "UTF-8");  

  }

  @Test
  public void testProcess_success() throws Exception {
    policy.process(getExchangeEvent("12345", "create", "generic-vnf"));
    policy.process(getExchangeEvent("23456", "create", "generic-vnf"));

    assertNotNull(
        InMemorySearchDatastore.get(NodeUtils.generateUniqueShaDigest("generic-vnf/12345")));
    assertNotNull(
        InMemorySearchDatastore.get(NodeUtils.generateUniqueShaDigest("generic-vnf/23456")));

   
    policy.process(getExchangeEvent("23456", "delete", "generic-vnf"));
    assertNull(InMemorySearchDatastore.get(NodeUtils.generateUniqueShaDigest("23456")));
  }
  @Test
  public void testProcess_fail() throws Exception {
    policy.process(getExchangeEvent("12345", "create", "NotValid"));
    assertNull(
        InMemorySearchDatastore.get(NodeUtils.generateUniqueShaDigest("NotValid/12345")));
    
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

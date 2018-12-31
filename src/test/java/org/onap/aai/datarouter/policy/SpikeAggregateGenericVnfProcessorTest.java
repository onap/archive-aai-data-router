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
import org.junit.runner.RunWith;
import org.onap.aai.datarouter.Application;
import org.onap.aai.datarouter.util.NodeUtils;
import org.onap.aai.setup.SchemaLocationsBean;
import org.onap.aai.setup.SchemaVersions;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Application.class})
public class SpikeAggregateGenericVnfProcessorTest {
  private SpikeEventPolicyConfig eventPolicyConfig;
  private SpikeAggregateGenericVnfProcessor policy;
  private InMemorySearchDatastore searchDb;

  @Autowired
  private SchemaVersions schemaVersions;
  @Autowired
  private SchemaLocationsBean schemaLocationsBean;

  @Before
  public void init() throws Exception {
    eventPolicyConfig = new SpikeEventPolicyConfig();
    eventPolicyConfig.setSearchKeystorePwd("password");
    eventPolicyConfig.setSourceDomain("JUNIT");

    eventPolicyConfig.setSchemaVersions(schemaVersions);
    eventPolicyConfig.setSchemaLocationsBean(schemaLocationsBean);

    searchDb = new InMemorySearchDatastore();
    policy = new SpikeAggregateGenericVnfProcessorStubbed(eventPolicyConfig).withSearchDb(searchDb);
  }

  @Test
  public void testProcess_success() throws Exception {

    String genericVnfEventJsonTemplate = IOUtils.toString(
        new FileInputStream(new File("src/test/resources/generic-vnf-spike-event.json")), "UTF-8");

    policy.process(
        getExchangeEvent(genericVnfEventJsonTemplate, "update-notification", "CREATE", "gvnf123"));

    assertNotNull(searchDb.get(NodeUtils.generateUniqueShaDigest("generic-vnf/gvnf123")));

    policy.process(
        getExchangeEvent(genericVnfEventJsonTemplate, "update-notification", "DELETE", "gvnf123"));

    assertNull(searchDb.get(NodeUtils.generateUniqueShaDigest("generic-vnf/gvnf123")));

    
  }
  /*
   * Failure test cases - no searchable attribute for type
   */

  @Test
  public void testProcess_failure_unknownOxmEntityType() throws Exception {

    String pserverEventJsonTemplate = IOUtils.toString(
        new FileInputStream(new File("src/test/resources/optical-router-spike-event.json")),
        "UTF-8");

    policy.process(
        getExchangeEvent(pserverEventJsonTemplate, "update-notification", "CREATE", "optronic123"));

    assertNull(searchDb.get(NodeUtils.generateUniqueShaDigest("optical-router/optronic123")));
  }

  @Test
  public void testProcess_failure_missingMandatoryFieldsFromBodyObject() throws Exception {

    String pserverEventJsonTemplate = IOUtils.toString(
        new FileInputStream(
            new File("src/test/resources/pserver-missing-mandtory-field-spike-event.json")),
        "UTF-8");

    policy.process(
        getExchangeEvent(pserverEventJsonTemplate, "update-notification", "CREATE", "pserver123"));

    assertNull(searchDb.get(NodeUtils.generateUniqueShaDigest("pserver/pserver123")));
  }

  private Exchange getExchangeEvent(String payloadTemplate, String eventType, String operationType,
      String entityKey) {
    Object obj = payloadTemplate.replace("$EVENT_TYPE", eventType)
        .replace("$OPERATION_TYPE", operationType).replace("$ENTITY_KEY", entityKey);

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

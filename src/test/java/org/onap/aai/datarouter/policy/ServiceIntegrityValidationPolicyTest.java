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



public class ServiceIntegrityValidationPolicyTest {
	private ServiceIntegrityValidationPolicy policy;
	private String eventJson;
	private String validationJson;
	private String violationjson;

	private InMemorySearchDatastore searchDb;

	@SuppressWarnings("unchecked")
	@Before
	public void init() throws Exception {

		String searchCertPath = "";
		String searchCertTruststore ="";
		String searchCertPassword = "password";
		String searchBaseURL = "";
		String endpoint = "services/search-data-service/v1/search/indexes/";
		String validationIndexName = "service-validations";
		String violationIndexName = "service-violations";


		searchDb = new InMemorySearchDatastore();
		policy = new ServiceIntegrityValidationPolicyStubbed(searchCertPath, searchCertTruststore,
				searchCertPassword, searchBaseURL, endpoint, validationIndexName, violationIndexName).withSearchDb(searchDb);

		FileInputStream event = new FileInputStream( new File("src/test/resources/poa_audit_result.json"));
		eventJson = IOUtils.toString(event, "UTF-8");

		FileInputStream validation = new FileInputStream( new File("src/test/resources/poa_auditservice_validation.json"));
		validationJson = IOUtils.toString(validation, "UTF-8");

		FileInputStream violation = new FileInputStream( new File("src/test/resources/poa_auditservice_violation.json"));
		violationjson = IOUtils.toString(violation, "UTF-8");


	}

	@Test
	public void testProcess() throws Exception {

		policy.process(getExchangeEvent(validationJson));
		policy.process(getExchangeEvent(violationjson));

		assertNotNull(searchDb.get("service-validations"));
		assertNotNull(searchDb.get("service-violations"));

	}



	private Exchange getExchangeEvent(String outputJson){

		Exchange exchange = PowerMockito.mock(Exchange.class);
		Message inMessage = PowerMockito.mock(Message.class);
		Message outMessage = PowerMockito.mock(Message.class);
		PowerMockito.when(exchange.getIn()).thenReturn(inMessage);
		PowerMockito.when(inMessage.getBody()).thenReturn(eventJson);

		PowerMockito.when(exchange.getOut()).thenReturn(outMessage);
		PowerMockito.when(outMessage.getBody()).thenReturn(outputJson);

		PowerMockito.doNothing().when(outMessage).setBody(anyObject());
		PowerMockito.doNothing().when(outMessage).setHeader(anyString(), anyObject());

		return exchange;

	}



}

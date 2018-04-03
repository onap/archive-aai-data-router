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
package org.onap.aai.datarouter.query;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aai.restclient.client.OperationResult;
import org.onap.aai.restclient.client.RestClient;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.gson.JsonParser;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ChameleonRouter.class, RestClient.class })
public class ChameleonRouterTest {

  ChameleonRouter chameleonRouter;
  RestClient mockRestClient = mock(RestClient.class);

  JsonParser parser = new JsonParser();

  @SuppressWarnings("unchecked")
  @Before
  public void init() throws Exception {
    RestClientConfig config = PowerMockito.mock(RestClientConfig.class);
    PowerMockito.when(config.getCertPassword()).thenReturn("password");

    PowerMockito.whenNew(RestClient.class).withAnyArguments().thenReturn(mockRestClient);
    PowerMockito.when(mockRestClient.validateServerHostname(any(Boolean.class))).thenReturn(mockRestClient);
    PowerMockito.when(mockRestClient.validateServerCertChain(any(Boolean.class))).thenReturn(mockRestClient);
    PowerMockito.when(mockRestClient.clientCertFile(any(String.class))).thenReturn(mockRestClient);
    PowerMockito.when(mockRestClient.clientCertPassword(any(String.class))).thenReturn(mockRestClient);
    PowerMockito.when(mockRestClient.trustStore(any(String.class))).thenReturn(mockRestClient);
    PowerMockito.when(mockRestClient.connectTimeoutMs(any(Integer.class))).thenReturn(mockRestClient);
    PowerMockito.when(mockRestClient.readTimeoutMs(any(Integer.class))).thenReturn(mockRestClient);

    chameleonRouter = new ChameleonRouter("http:///test", config);
  }

  @Test
  public void testProcess() throws Exception {
    OperationResult chameleonResponse = buildChameleonResponse();
    PowerMockito.when(mockRestClient.get(any(String.class), any(HashMap.class), any(MediaType.class)))
        .thenReturn(chameleonResponse);

    String chameleonRouterResponse = chameleonRouter.process("/objects/364d646e-c947-4010-a66a-adf06aa306fb", "", null);
    Assert.assertEquals(parser.parse(chameleonRouterResponse), parser.parse(readSampleChampResponse()));

  }

  private OperationResult buildChameleonResponse() throws IOException {
    OperationResult response = new OperationResult();
    response.setResultCode(200);

    response.setResult(readSampleChameleonResponse());
    return response;
  }

  private String readSampleChampResponse() throws IOException {
    FileInputStream event = new FileInputStream(new File("src/test/resources/champ-response.json"));
    String json = IOUtils.toString(event, "UTF-8");
    return json;
  }

  private String readSampleChameleonResponse() throws IOException {
    FileInputStream event = new FileInputStream(new File("src/test/resources/chameleon-response.json"));
    String json = IOUtils.toString(event, "UTF-8");
    return json;
  }

}

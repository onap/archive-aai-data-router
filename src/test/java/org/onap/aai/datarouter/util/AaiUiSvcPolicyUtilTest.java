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
package org.onap.aai.datarouter.util;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.aai.datarouter.util.AaiUiSvcPolicyUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AaiUiSvcPolicyUtilTest {
  
  private final static String ORIGIN_URI = "testUri/somePath";
  private final static String ORIGIN_PAYLOAD = "test payload";

  private final static String validPayload = "{" +
        "\"origin-uri\": \"" + ORIGIN_URI + "\"," + 
        "\"origin-payload\": \"" + ORIGIN_PAYLOAD + "\"}";
  
  private final static String payloadWithoutOriginUri = "{" +
      "\"origin-payload\": \"" + ORIGIN_PAYLOAD + "\"}";
  
  private final static String payloadWithoutOriginPayload = "{" +
      "\"origin-uri\": \"" + ORIGIN_URI + "\"}";
  
  private static JsonNode node = null;
  private static JsonNode nodeWithoutOrginUri = null;
  private static JsonNode nodeWithoutOrginPayload = null;
  static ObjectMapper mapper = new ObjectMapper();
  
  @BeforeClass
  public static void init(){
    try {
      node = mapper.readTree(validPayload);
      nodeWithoutOrginUri = mapper.readTree(payloadWithoutOriginUri);
      nodeWithoutOrginPayload = mapper.readTree(payloadWithoutOriginPayload);
    } catch (Exception e) {
      fail("Initialization error");
    }
  }
  
  @Test
  public void testGetOriginPayload_missingPayload() {
    JsonNode value = null;
    try {
      value = AaiUiSvcPolicyUtil.getOriginPayload(nodeWithoutOrginPayload);
      assertNull("Failure to extract origin payload", value);
    } catch (Exception e) {
      fail("Failure to extract origin payload");
    }
  }

  @Test
  public void testGetOriginPayload_validPayload() {
    JsonNode value = null;
    try {
      value = AaiUiSvcPolicyUtil.getOriginPayload(node);
      assertTrue("Failure to extract origin payload", ORIGIN_PAYLOAD.equals(value.asText()));
    } catch (Exception e) {
      fail("Failure to extract origin payload");
    }
  }
  
  @Test
  public void testGetOriginUri_missingUri() {
    String value = null;
    try {
      value = AaiUiSvcPolicyUtil.getOriginUri(nodeWithoutOrginUri);
      assertTrue("Failure to extract origin uri", value.isEmpty());
    } catch (Exception e) {
      fail("Failure to extract origin uri");
    }
  }

  @Test
  public void testGetOriginUri_validPayload() {
    String value = null;
    try {
      value = AaiUiSvcPolicyUtil.getOriginUri(node);
      assertTrue("Failure to extract origin uri", ORIGIN_URI.equals(value));
    } catch (Exception e) {
      fail("Failure to extract origin uri");
    }
  }
}

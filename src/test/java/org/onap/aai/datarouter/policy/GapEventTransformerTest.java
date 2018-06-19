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

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;



public class GapEventTransformerTest {
  GapEventTransformer policy;
  String eventJson;
  String eventJson2;

  @SuppressWarnings("unchecked")
  @Before
  public void init() throws Exception {
    policy = new GapEventTransformer();
    FileInputStream event = new FileInputStream(new File("src/test/resources/gap_event.json"));
    eventJson = IOUtils.toString(event, "UTF-8");
    FileInputStream event2 = new FileInputStream(new File("src/test/resources/gap_event_wrong.json"));
    eventJson2 = IOUtils.toString(event2, "UTF-8");

  }

  @Test
  public void testTransform_success() throws Exception {
    JSONObject newPayloadJson = policy.transformToSpikePattern(eventJson.toString());
    JSONObject payloadEntity = newPayloadJson.getJSONObject("vertex");
    assertTrue(newPayloadJson.has("vertex"));
    assertTrue(newPayloadJson.has("operation"));
    assertEquals(newPayloadJson.get("operation"), "UPDATE");
  

  }

  @Test(expected = Exception.class) 
  public void testTransform_badPayload_fail() throws Exception {
     policy.transformToSpikePattern(eventJson2.toString());
  }

}

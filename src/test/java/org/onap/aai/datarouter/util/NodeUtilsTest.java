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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class NodeUtilsTest {

    @Test
    public void testGenerateUniqueShaDigest_NullParam(){
        NodeUtils utils = new NodeUtils();
        String keys=null;
        String hashdId = NodeUtils.generateUniqueShaDigest();
        Assert.assertNull(hashdId);
    }

    @Test
    public void testExtractFieldValueFromObject_NullNode_ExpectNullString(){
        JsonNode node = null;
        String valueNode = NodeUtils.extractFieldValueFromObject(node, "field-1");
        Assert.assertNull(valueNode);
    }

    @Test
    public void testExtractFieldValueFromObject_NotNullNode_ExpectNotNullString() throws IOException {
        String jsonStr = "{\"key\":\"value\"}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(jsonStr);
        String valueNode = NodeUtils.extractFieldValueFromObject(node, "key");
        Assert.assertEquals(valueNode, "value");
    }

    @Test
    public void testConvertJsonStrToJsonNode_NullJsonStr() throws IOException {
        String jsonStr = null;
        JsonNode jsonNode = NodeUtils.convertJsonStrToJsonNode(jsonStr);
        Assert.assertNull(jsonNode);
    }

    @Test
    public void testExtractObjectsByKey() throws IOException {
        String jsonStr = "{\n" +
                "  \"id\"   : 1,\n" +
                "  \"name\" : {\n" +
                "    \"first\" : \"user\",\n" +
                "    \"last\" : \"name\"\n" +
                "  },\n" +
                "  \"contact\" : [\n" +
                "    { \"type\" : \"phone/home\", \"ref\" : \"111-111-1234\"},\n" +
                "    { \"type\" : \"phone/work\", \"ref\" : \"222-222-2222\"}\n" +
                "  ]\n" +
                "}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(jsonStr);
        List arrList = new ArrayList();
        NodeUtils.extractObjectsByKey(node, "name", arrList);
    }

    @Test
    public void testConvertObjectToJson() throws IOException {
        String jsonStr = "{\"key\":\"value\"}";
        ObjectMapper mapper = new ObjectMapper();
        Object node = mapper.readTree(jsonStr);
        String retNode1 = NodeUtils.convertObjectToJson(node, false);
        Assert.assertNotNull(retNode1);
        String retNode2 = NodeUtils.convertObjectToJson(node, true);
        Assert.assertNotNull(retNode2);
    }
}
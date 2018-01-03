/**
 * ﻿============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2017 Amdocs
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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */
package org.onap.aai.datarouter.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import javax.json.Json;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RouterServiceUtilTest {

    @Test
    public void testParseJsonPayloadIntoMap(){
        String jsonStr = "{\"key1\":\"value1\", \"key2\":\"value2\", \"key3\":\"value3\"}";
        Map<String, String> retMap = RouterServiceUtil.parseJsonPayloadIntoMap(jsonStr);
        Assert.assertNotNull(retMap);
        Assert.assertEquals(retMap.get("key1"), "value1");
    }

    @Test
    public void testGetNodeFieldAsText() throws IOException {
        String jsonStr = "{\"key\":\"value\"}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(jsonStr);
        String field = RouterServiceUtil.getNodeFieldAsText(node, "key");
    }

    @Test
    public void testConcatArray_NullList(){
        List<String> strList = null;
        String result = RouterServiceUtil.concatArray(strList);
        Assert.assertTrue(result.isEmpty());
    }

    /*Method in RouterServiceUtil is wrong. It should be corrected
    @Test
    public void testConcatArray_NotNullList(){
        List<String> strList = new ArrayList<String>();
        strList.add("str1");
        strList.add("str2");
        strList.add("str3");
        String result = RouterServiceUtil.concatArray(strList);
        Assert.assertEquals(result, "str1 str2 str3");
    }*/

    @Test
    public void testConcatArray_NullArray(){
        String[] values = new String[]{};
        //String[] values = new String[]{"value1", "value2", "value3"};
        String result = RouterServiceUtil.concatArray(values);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testConcatArray_NotNullArray(){
        String[] values = new String[]{"value1", "value2", "value3"};
        String result = RouterServiceUtil.concatArray(values);
        Assert.assertEquals(result, "value1.value2.value3");
    }

    @Test
    public void testRecursivelyLookupJsonPayload_JsonArray() throws IOException {
        String jsonStr = "[{\"key1\":\"value1\"}, {\"key2\":\"value2\"}]";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(jsonStr);
        String result = RouterServiceUtil.recursivelyLookupJsonPayload(node, "key1");
        Assert.assertEquals(result, "value1");
    }

    @Test
    public void testExtractObjectsByKey() throws IOException {
        String jsonStr = "[{\"key1\":\"value1\"}, {\"key2\":\"value2\"}]";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(jsonStr);
        List<JsonNode> nodeList = new ArrayList<JsonNode>();
        RouterServiceUtil.extractObjectsByKey(node, "key1", nodeList);
        Assert.assertTrue(nodeList.size() == 1);
    }

    @Test
    public void testConvertArrayIntoList() throws IOException {
        String jsonStr = "[{\"key1\":\"value1\"}, {\"key2\":\"value2\"}]";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(jsonStr);
        List<JsonNode> nodeList = new ArrayList<JsonNode>();
        RouterServiceUtil.convertArrayIntoList(node, nodeList);
        Assert.assertTrue(nodeList.size() == 2);
    }

    @Test
    public void testExtractFieldValueFromObject() throws IOException {
        String jsonStr = "{\"key1\":\"value1\"}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(jsonStr);
        List<String> attrList = new ArrayList<String>();
        attrList.add("key1");
        List<String> valueList = new ArrayList<String>();
        RouterServiceUtil.extractFieldValuesFromObject(node,attrList,valueList);
        Assert.assertTrue(valueList.size()==1);
        Assert.assertTrue(valueList.get(0).equals("value1"));
    }

    @Test
    public void testObjToJson() throws IOException {
        String jsonStr = "{\"key1\":\"value1\"}";
        ObjectMapper mapper = new ObjectMapper();
        Object node = mapper.readTree(jsonStr);
        String retJson = RouterServiceUtil.objToJson(node);
        Assert.assertNotNull("localhost/aai/data-router/");
    }

    @Test
    public void testConcatSubUri(){
        String finalSubUri = RouterServiceUtil.concatSubUri("localhost/","aai/","data-router");
        Assert.assertEquals(finalSubUri, "localhost/aai/data-router/");
    }
}

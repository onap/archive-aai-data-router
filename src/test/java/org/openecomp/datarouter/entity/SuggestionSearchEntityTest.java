/**
 * ============LICENSE_START=======================================================
 * DataRouter
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property.
 * Copyright © 2017 Amdocs
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 * ECOMP and OpenECOMP are trademarks
 * and service marks of AT&T Intellectual Property.
 */
package org.openecomp.datarouter.entity;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.aai.datarouter.entity.SuggestionSearchEntity;
import org.onap.aai.datarouter.search.filters.config.UiFiltersSchemaUtility;

public class SuggestionSearchEntityTest {
  private static SuggestionSearchEntity suggestionSearchEntity;

  @Before
  public void setUpBeforeTest() {
    UiFiltersSchemaUtility filtersSchemaUtility = Mockito.mock(UiFiltersSchemaUtility.class);
    Mockito.when(filtersSchemaUtility.loadUiFiltersConfig()).thenReturn(null);

    suggestionSearchEntity = new SuggestionSearchEntity();
    suggestionSearchEntity.setFiltersSchemaUtility(filtersSchemaUtility);
    suggestionSearchEntity.setEntityType("generic-vnf");
    suggestionSearchEntity.setEntityTypeAliases(Arrays.asList("VNFs"));
  }

  /**
   * Read in the contents of the given file (can include sub-path) in test/resources folder
   *
   * @param filePath The file name or path (relative to test/resources) to read from
   * @return The contents of the file as a String
   */
  public String getResourceFileContents(String filePath) {
    StringBuilder result = new StringBuilder("");

    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource(filePath).getFile());

    try (Scanner scanner = new Scanner(file)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        result.append(line).append("\n");
      }

      scanner.close();

    } catch (IOException e) {
      e.printStackTrace();
    }

    return result.toString();
  }

  @Test
  public void testGetAsJson_multipleFilterAttributableStatusesIncluded() throws IOException {
    String expectedOutput =
      getResourceFileContents("uifilters/testGetAsJson_multipleFilterAttributableStatusesIncluded_expectedValue.json");

    List<String> suggestionInputPermutations = Arrays.asList(
        "provStatus1 orchestrationStatus1 generic-vnf",
        "provStatus1 generic-vnf orchestrationStatus1",
        "orchestrationStatus1 generic-vnf provStatus1",
        "orchestrationStatus1 provStatus1 generic-vnf",
        "generic-vnf provStatus1 orchestrationStatus1",
        "generic-vnf orchestrationStatus1 provStatus1");

    Map<String, String>inputOutputData = new HashMap<>();
    inputOutputData.put("prov-status", "provStatus1");
    inputOutputData.put("orchestration-status", "orchestrationStatus1");

    // Build UI filters JSON string
    JSONObject payloadFilter1 = new JSONObject();
    payloadFilter1.put("filterId", "1");
    payloadFilter1.put("filterValue", "orchestrationStatus1");

    JSONObject payloadFilter2 = new JSONObject();
    payloadFilter2.put("filterId", "2");
    payloadFilter2.put("filterValue", "provStatus1");

    JSONArray payloadFilters = new JSONArray();
    payloadFilters.put(payloadFilter2);
    payloadFilters.put(payloadFilter1);

    JSONObject filterPayload = new JSONObject();
    filterPayload.put("filterList", payloadFilters);

    suggestionSearchEntity.setSuggestionInputPermutations(suggestionInputPermutations);
    suggestionSearchEntity.setInputOutputData(inputOutputData);
    suggestionSearchEntity.setFilterPayload(filterPayload);

    String actualOutput = suggestionSearchEntity.getAsJson();

    assertEquals(expectedOutput.trim(), actualOutput.trim());
  }

  @Test
  public void testGetAsJson_singleFilterAttributableStatusIncluded() throws IOException {
    String expectedOutput =
      getResourceFileContents("uifilters/testGetAsJson_singleFilterAttributableStatusIncluded_expectedValue.json");

    List<String> suggestionInputPermutations = Arrays.asList(
        "provStatus1 generic-vnf",
        "generic-vnf provStatus1");

    Map<String, String>inputOutputData = new HashMap<>();
    inputOutputData.put("prov-status", "provStatus1");

    // Build UI filters JSON string
    JSONObject payloadFilter1 = new JSONObject();
    payloadFilter1.put("filterId", "2");
    payloadFilter1.put("filterValue", "provStatus1");

    JSONArray payloadFilters = new JSONArray();
    payloadFilters.put(payloadFilter1);

    JSONObject filterPayload = new JSONObject();
    filterPayload.put("filterList", payloadFilters);

    suggestionSearchEntity.setSuggestionInputPermutations(suggestionInputPermutations);
    suggestionSearchEntity.setInputOutputData(inputOutputData);
    suggestionSearchEntity.setFilterPayload(filterPayload);

    String actualOutput = suggestionSearchEntity.getAsJson();

    assertEquals(expectedOutput.trim(), actualOutput.trim());
  }
}

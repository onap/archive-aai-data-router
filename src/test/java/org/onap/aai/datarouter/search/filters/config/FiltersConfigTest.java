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
package org.onap.aai.datarouter.search.filters.config;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FiltersConfigTest {

    @Test
    public void testUiFiltersConfig_AllMethods(){

        UiFilterDataSourceConfig dsConfig = getDataSourceConfig();
        UiFilterConfig filterConfig = getFilterConfig(dsConfig);

        List<UiFilterConfig> filters = new ArrayList<UiFilterConfig>();
        filters.add(filterConfig);
        UiFiltersConfig config1 = new UiFiltersConfig(filters);
        Assert.assertNotNull(config1);
        UiFiltersConfig config2 = new UiFiltersConfig();
        config2.setFilters(filters);
        Assert.assertTrue(config2.getFilters().size()==1);
        Assert.assertNotNull(config2.toString());
    }

    private UiFilterConfig getFilterConfig(UiFilterDataSourceConfig dsConfig) {
        UiFilterConfig filterConfig = new UiFilterConfig("id-1", "name-1",
                "display-1", "dataType-1", dsConfig);
        filterConfig.setFilterId("id-1");
        Assert.assertEquals("id-1", filterConfig.getFilterId());
        filterConfig.setFilterName("name-1");
        Assert.assertEquals("name-1", filterConfig.getFilterName());
        filterConfig.setDisplayName("display-1");
        Assert.assertEquals("display-1", filterConfig.getDisplayName());
        filterConfig.setDataType("dataType-1");
        Assert.assertEquals("dataType-1", filterConfig.getDataType());
        filterConfig.setDataSource(dsConfig);
        Assert.assertEquals(dsConfig, filterConfig.getDataSource());
        Assert.assertNotNull(filterConfig.toString());
        return filterConfig;
    }

    private UiFilterDataSourceConfig getDataSourceConfig() {
        UiFilterDataSourceConfig dsConfig = new UiFilterDataSourceConfig("index-1", "doctype-1",
                "field-1", "path-1");
        dsConfig.setIndexName("index-1");
        Assert.assertEquals("index-1", dsConfig.getIndexName());
        dsConfig.setDocType("doctype-1");
        Assert.assertEquals("doctype-1", dsConfig.getDocType());
        dsConfig.setFieldName("field-1");
        Assert.assertEquals("field-1", dsConfig.getFieldName());
        dsConfig.setPathToField("path-1");
        Assert.assertEquals("path-1", dsConfig.getPathToField());
        Assert.assertNotNull(dsConfig.toString());
        return dsConfig;
    }
}

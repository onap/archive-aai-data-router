/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Samsung Electronics Co., Ltd. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.aai.datarouter.util;

import java.util.List;
import org.apache.camel.Exchange;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.onap.aai.datarouter.entity.SpikeEventEntity;

public class SpikeEventEntityBuilder {

    private Exchange exchange;
    private SpikeEventEntity spikeEventEntity;
    private DynamicJAXBContext oxmJaxbContext;

    private String entityType;
    private String action;
    private String uebPayload;
    private String oxmEntityType;
    private List<String> searchableAttr;

    public Exchange getExchange() {
        return exchange;
    }

    public SpikeEventEntityBuilder setExchange(Exchange exchange) {
        this.exchange = exchange;
        return this;
    }

    public SpikeEventEntity getSpikeEventEntity() {
        return spikeEventEntity;
    }

    public SpikeEventEntityBuilder setSpikeEventEntity(SpikeEventEntity spikeEventEntity) {
        this.spikeEventEntity = spikeEventEntity;
        return this;
    }

    public DynamicJAXBContext getOxmJaxbContext() {
        return oxmJaxbContext;
    }

    public SpikeEventEntityBuilder setOxmJaxbContext(DynamicJAXBContext oxmJaxbContext) {
        this.oxmJaxbContext = oxmJaxbContext;
        return this;
    }

    public String getEntityType() {
        return entityType;
    }

    public SpikeEventEntityBuilder setEntityType(String entityType) {
        this.entityType = entityType;
        return this;
    }

    public String getAction() {
        return action;
    }

    public SpikeEventEntityBuilder setAction(String action) {
        this.action = action;
        return this;
    }

    public String getUebPayload() {
        return uebPayload;
    }

    public SpikeEventEntityBuilder setUebPayload(String uebPayload) {
        this.uebPayload = uebPayload;
        return this;
    }

    public String getOxmEntityType() {
        return oxmEntityType;
    }

    public SpikeEventEntityBuilder setOxmEntityType(String oxmEntityType) {
        this.oxmEntityType = oxmEntityType;
        return this;
    }

    public List<String> getSearchableAttr() {
        return searchableAttr;
    }

    public SpikeEventEntityBuilder setSearchableAttr(List<String> searchableAttr) {
        this.searchableAttr = searchableAttr;
        return this;
    }


}

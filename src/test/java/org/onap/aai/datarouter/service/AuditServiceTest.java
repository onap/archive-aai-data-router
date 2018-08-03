/**
 * ============LICENSE_START=======================================================
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
package org.onap.aai.datarouter.service;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.onap.aai.datarouter.entity.POAServiceInstanceEntity;
import org.onap.aai.datarouter.event.POAAuditEvent;


public class AuditServiceTest {

    private String eventJson;


    @Before
    public void init() throws Exception {
        FileInputStream event = new FileInputStream( new File("src/test/resources/poa_event.json"));
        eventJson = IOUtils.toString(event, "UTF-8");
    }

    @Test
    public void testPOAEvent() throws Exception {

        POAAuditEvent auditEvent = POAAuditEvent.fromJson(eventJson);

        List<String> eventMessages = new ArrayList<String>();
        for (POAServiceInstanceEntity serviceInstance: auditEvent.getServiceInstanceList()) {
            serviceInstance.validate();
            serviceInstance.setxFromAppId("data-router");
            serviceInstance.setxTransactionId("111222888");
            eventMessages.add(serviceInstance.toJson());
        }

        assertEquals(2, eventMessages.size());
    }



}

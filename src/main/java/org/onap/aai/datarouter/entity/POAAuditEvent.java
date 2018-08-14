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
package org.onap.aai.datarouter.entity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.datarouter.exception.POAAuditException;
import org.onap.aai.datarouter.logging.DataRouterMsgs;

/**
 * Wrapper class for POMBA rest call message body
 *
 */
public class POAAuditEvent
{
    private static final String ATTR_SERVICE_INST_LIST = "serviceInstanceList";

    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private List<POAServiceInstanceEntity> serviceInstanceList = new ArrayList<POAServiceInstanceEntity>();

    private static Logger logger = LoggerFactory.getInstance().getLogger(POAAuditEvent.class.getName());

    public String toJson() {
        return gson.toJson(this);
    }

    public static POAAuditEvent fromJson(String payload) throws POAAuditException {
       if (payload == null || payload.isEmpty()) {
            throw new POAAuditException("Invalid Json Payload", Status.BAD_REQUEST);
        }

        try {
            return gson.fromJson(payload, POAAuditEvent.class);
        } catch (Exception ex) {
            logger.debug("Invalid Json Payload: " + payload);
            throw new POAAuditException("Invalid Json Payload", Status.BAD_REQUEST, DataRouterMsgs.BAD_REST_REQUEST, ex.getMessage());
        }
    }

    public List<POAServiceInstanceEntity> getServiceInstanceList() {
        return serviceInstanceList;
    }

    public void setServiceInstanceList(List<POAServiceInstanceEntity> instances) {
        this.serviceInstanceList = instances;
    }


    /**
     * Validates this service instance list
     * @throws POAAuditException if the list is absent or empty
     */
    public void validate() throws POAAuditException {

        if((serviceInstanceList == null) || serviceInstanceList.isEmpty()) {
            String error = "Missing attribute list: " + ATTR_SERVICE_INST_LIST;
            throw new POAAuditException(error, Status.BAD_REQUEST, DataRouterMsgs.BAD_REST_REQUEST, error);
        }
    }

    @Override
    public String toString() {
        return "AuditEvent [serviceInstances=" + serviceInstanceList + "]";
    }
}

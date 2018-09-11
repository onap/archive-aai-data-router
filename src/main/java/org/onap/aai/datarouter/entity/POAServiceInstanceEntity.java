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
import javax.ws.rs.core.Response.Status;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.datarouter.exception.POAAuditException;
import org.onap.aai.datarouter.logging.DataRouterMsgs;
import org.onap.aai.datarouter.service.AuditService;

/**
 * This class includes the POMBA rest call message body which triggers the audit process
 *
 */
public class POAServiceInstanceEntity
{
    private static final String ATTR_SERVICE_INST_ID = "serviceInstanceId";
    private static final String ATTR_MODEL_VERSION_ID = "modelVersionId";
    private static final String ATTR_MODEL_INVARIANT_ID = "modelInvariantId";

    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    private String xFromAppId;
    private String xTransactionId;
    private String serviceInstanceId;
    private String modelVersionId;
    private String modelInvariantId;

    private static Logger logger = LoggerFactory.getInstance().getLogger(POAServiceInstanceEntity.class.getName());


    public String getxFromAppId() {
        return xFromAppId;
    }

    public void setxFromAppId(String xFromAppId) {
        this.xFromAppId = xFromAppId;
    }

    public String getxTransactionId() {
        return xTransactionId;
    }

    public void setxTransactionId(String xTransactionId) {
        this.xTransactionId = xTransactionId;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public String getModelVersionId() {
        return modelVersionId;
    }

    public void setModelVersionId(String modelVersionId) {
        this.modelVersionId = modelVersionId;
    }

    public String getModelInvariantId() {
        return modelInvariantId;
    }

    public void setModelInvariantId(String modelInvariantId) {
        this.modelInvariantId = modelInvariantId;
    }

    public String toJson() {
        return gson.toJson(this);
    }


    public static POAServiceInstanceEntity fromJson(String payload) throws POAAuditException {

        if (payload == null || payload.isEmpty()) {
            throw new POAAuditException("Invalid Json Payload", Status.BAD_REQUEST);
        }

        try {
            return gson.fromJson(payload, POAServiceInstanceEntity.class);
        } catch (Exception ex) {
            logger.debug("Invalid Json Payload: " + payload);
            throw new POAAuditException("Invalid Json Payload", Status.BAD_REQUEST, DataRouterMsgs.BAD_REST_REQUEST, ex.getMessage());
        }
    }


    /**
     * Validates this service instance; only the attributes that are expected in request body are verified.
     * @throws POAAuditException if the service instance is invalid
     */
    public void validate() throws POAAuditException {

        if(serviceInstanceId == null || serviceInstanceId.isEmpty()) {
            String error = "Missing attribute: " + ATTR_SERVICE_INST_ID;
            throw new POAAuditException(error, Status.BAD_REQUEST, DataRouterMsgs.BAD_REST_REQUEST, error);
        }

        if(modelVersionId == null || modelVersionId.isEmpty()) {
            String error = "Missing attribute: " + ATTR_MODEL_VERSION_ID;
            throw new POAAuditException(error, Status.BAD_REQUEST, DataRouterMsgs.BAD_REST_REQUEST, error);
        }

        if(modelInvariantId == null || modelInvariantId.isEmpty()) {
            String error = "Missing attribute: " + ATTR_MODEL_INVARIANT_ID;
            throw new POAAuditException(error, Status.BAD_REQUEST, DataRouterMsgs.BAD_REST_REQUEST, error);
        }

    }


    @Override
    public String toString() {
        return "ServiceInstance [xFromAppId=" + xFromAppId + ", xTransactionId=" + xTransactionId
                + ", serviceInstanceId=" + serviceInstanceId + ", modelVersionId="
                + modelVersionId + ", modelInvariantId=" + modelInvariantId + "]";
    }
}

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Encoded;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.cl.mdc.MdcContext;
import org.onap.aai.datarouter.entity.POAAuditEvent;
import org.onap.aai.datarouter.entity.POAServiceInstanceEntity;
import org.onap.aai.datarouter.exception.POAAuditException;
import org.onap.aai.datarouter.logging.DataRouterMsgs;
import org.onap.aai.datarouter.util.LoggingUtil;
import org.onap.aai.event.api.EventPublisher;
import org.onap.aai.datarouter.util.DataRouterConstants;
import org.onap.aai.restclient.client.Headers;

public class AuditService {

    private static Logger logger = LoggerFactory.getInstance().getLogger(AuditService.class.getName());
    private static Logger auditLogger = LoggerFactory.getInstance().getAuditLogger(AuditService.class.getName());

    private static final String MEDIA_TYPE = MediaType.APPLICATION_JSON;
    private static final String UNKNOWN_APP = "[unknown_app_id]";
    private static final String RESULT_OK = "200 OK";

    private EventPublisher publisher;

    public AuditService(EventPublisher publisher) {
        this.publisher = publisher;
    }


    /**
     * Handles an incoming post-orchestration audit request.
     * Generates and posts audit events to DMaaP.
     *
     * @param content - JSON structure containing the request payload
     * @param uri     - Http request uri
     * @param headers - Http request headers
     * @param uriInfo - Http URI info field
     * @param req     - Http request structure.
     * @return - Standard HTTP response.
     *
     */
    @POST
    @Path("/orchestration-event/")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response triggerPOA(String content,
            @PathParam("uri") @Encoded String uri, @Context HttpHeaders headers, @Context UriInfo uriInfo,
            @Context HttpServletRequest req) {

        logger.debug("Incoming request..." + content);
        Response response = null;
        try {
            Map<String, String> headerAttributes = initializeLogging(req, headers);

            List<String> serviceInstanceEvents = generateServiceInstanceEvents(headerAttributes, content);
            processEvent(serviceInstanceEvents);

            response = Response.status(Status.OK).entity(RESULT_OK).type(MEDIA_TYPE).build();
            LoggingUtil.logRestRequest(logger, auditLogger, req, response);

        } catch(POAAuditException e) {
            response = Response.status(e.getHttpStatus()).entity(e.getMessage()).build();
            LoggingUtil.logRestRequest(logger, auditLogger, req, response, e);
        } finally {
            LoggingUtil.closeMdc();
        }
        return response;
    }


    /**
     * Initialize MDC logging using attributes from the request
     * @param httpReq
     * @param httpHeaders
     * @return Returns a map of required header attributes
     * @throws POAAuditException
     */
    private Map<String, String> initializeLogging(HttpServletRequest httpReq, HttpHeaders httpHeaders) throws POAAuditException {

        Map<String, String> headers = new HashMap<String, String>();
        String remoteAddr = httpReq.getRemoteAddr();

        if (httpHeaders.getRequestHeaders() == null) {
            String error="Missing Header";
            throw new POAAuditException(error, Status.BAD_REQUEST, DataRouterMsgs.BAD_REST_REQUEST, error);
        }
        String transactionId = httpHeaders.getRequestHeaders().getFirst(Headers.TRANSACTION_ID);
        if((transactionId == null) || transactionId.trim().isEmpty()) {
            transactionId = UUID.randomUUID().toString();
            logger.debug("Header " + Headers.TRANSACTION_ID + "not present in request, generating new value: " + transactionId);
        }
        headers.put(Headers.TRANSACTION_ID, transactionId);

        String fromAppId = httpHeaders.getRequestHeaders().getFirst(Headers.FROM_APP_ID);
        if((fromAppId == null) || fromAppId.trim().isEmpty()) {
            // initialize the context before throwing the exception
            MdcContext.initialize(transactionId, DataRouterConstants.DATA_ROUTER_SERVICE_NAME, "", UNKNOWN_APP, remoteAddr);
            String error = "Missing header attribute: " + Headers.FROM_APP_ID;
            throw new POAAuditException(error, Status.BAD_REQUEST, DataRouterMsgs.BAD_REST_REQUEST, error);
        }
        headers.put(Headers.FROM_APP_ID, fromAppId);

        LoggingUtil.initMdc(transactionId, fromAppId, remoteAddr);

        return headers;
    }



    /**
     * Extracts service instances and generates a list of events
     * @param eventHeaders
     * @param requestBody
     * @return Returns a list of service instances
     * @throws POAAuditException if the request contains errors
     */
    private List<String> generateServiceInstanceEvents(Map<String, String> eventHeaders, String requestBody) throws POAAuditException {

        POAAuditEvent auditEvent = POAAuditEvent.fromJson(requestBody);
        auditEvent.validate();

        List<String> eventMessages = new ArrayList<String>();
        for (POAServiceInstanceEntity serviceInstance: auditEvent.getServiceInstanceList()) {
            serviceInstance.validate();
            serviceInstance.setxFromAppId(eventHeaders.get(Headers.FROM_APP_ID));
            serviceInstance.setxTransactionId(eventHeaders.get(Headers.TRANSACTION_ID));
            eventMessages.add(serviceInstance.toJson());
        }
        return eventMessages;
    }

    /**
     * Publish events to DMaaP.
     * @param eventMessages
     * @throws POAAuditException
     *
     * Note: The Default Transport Type in the DMaaPEventPublisher is defined as "HTTPAAF". Based on the deployment of DMaap, currently
     * by default the "HTTPAUTH" transport type is supported.
     */
    private void processEvent(List<String> eventMessages) throws POAAuditException {

        int messagesSent = 0;
        try {
            messagesSent = publisher.sendSync(eventMessages);
            logger.debug("The number of messages successfully sent: "+ messagesSent);

            if (messagesSent > 0) {
                logger.debug("Published Message: " + eventMessages);
            } else {
                String exceptionStr = String.format("Failed to publish %d event(s) to DMaaP", eventMessages.size());
                logger.debug(exceptionStr);
            }


        } catch (Exception e) {
            throw new POAAuditException("Failed to publish event to DMaaP: " + e.getMessage(), Status.INTERNAL_SERVER_ERROR,
                    DataRouterMsgs.EXCEPTION_DURING_METHOD_CALL, e.getMessage());
        }

    }
}


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
package org.onap.aai.datarouter.service;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.onap.aai.cl.api.LogFields;
import org.onap.aai.cl.api.LogLine;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.cl.mdc.MdcContext;
import org.onap.aai.datarouter.logging.DataRouterMsgs;
import org.onap.aai.datarouter.util.DataRouterConstants;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
@Path("/data-router/v1")
public class EchoService {

  private static Logger logger = LoggerFactory.getInstance().getLogger(EchoService.class.getName());
  private static Logger auditLogger =
      LoggerFactory.getInstance().getAuditLogger(EchoService.class.getName());
  private static final String XFROMAPPID = "X-FromAppId";
  private static final String XTRANSACTIONID = "X-TransactionId";

  @GET
  @Path("/echo/{input}")
  @Produces("text/plain")
  public String ping(@PathParam("input") String input, @Context HttpHeaders headers,
      @Context UriInfo info, @Context HttpServletRequest req) {

	String fromIp = req.getRemoteAddr();
    String fromAppId = "";
    String transId;

    if (headers.getRequestHeaders().getFirst(XFROMAPPID) != null) {
      fromAppId = headers.getRequestHeaders().getFirst(XFROMAPPID);
    }

    if ((headers.getRequestHeaders().getFirst(XTRANSACTIONID) == null)
        || headers.getRequestHeaders().getFirst(XTRANSACTIONID).isEmpty()) {
      transId = java.util.UUID.randomUUID().toString();
    } else {
      transId = headers.getRequestHeaders().getFirst(XTRANSACTIONID);
    }

    MdcContext.initialize(transId, DataRouterConstants.DATA_ROUTER_SERVICE_NAME, "", fromAppId,
        fromIp);
    
    int status = 200;
    String respStatusString = "";
    if (Response.Status.fromStatusCode(status) != null) {
      respStatusString = Response.Status.fromStatusCode(status).toString();
    }

    // Generate error log
    logger.info(DataRouterMsgs.PROCESS_REST_REQUEST, req.getMethod(),
        req.getRequestURL().toString(), req.getRemoteHost(), Integer.toString(status));

    // Generate audit log.
    auditLogger.info(DataRouterMsgs.PROCESS_REST_REQUEST,
        new LogFields().setField(LogLine.DefinedFields.RESPONSE_CODE, status)
            .setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION, respStatusString),
        req.getMethod(), req.getRequestURL().toString(), req.getRemoteHost(), Integer.toString(status));
    MDC.clear();

    return "Hello, " + input + ".";
  }
}

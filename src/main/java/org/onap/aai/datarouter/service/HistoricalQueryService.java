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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
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
import org.onap.aai.datarouter.exception.DataRouterException;
import org.onap.aai.datarouter.logging.DataRouterMsgs;
import org.onap.aai.datarouter.logging.LoggingUtil;
import org.onap.aai.datarouter.query.QueryRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Path("/services/champ-service/v1")
public class HistoricalQueryService {

  private Logger logger = LoggerFactory.getInstance().getLogger(HistoricalQueryService.class);
  Logger auditLogger = LoggerFactory.getInstance().getAuditLogger(HistoricalQueryService.class.getName());
  private static Logger metricsLogger = LoggerFactory.getInstance()
      .getMetricsLogger(HistoricalQueryService.class.getName());

  @Autowired
  @Qualifier("champ")
  private QueryRouter champRouter;

  @Autowired
  @Qualifier("chameleon")
  private QueryRouter chameleonRouter;

  @GET  
  @Path("{uri: .+}")
  @Produces({ MediaType.APPLICATION_JSON })
  public Response process(String content, @PathParam("version") String versionParam,
      @PathParam("uri") @Encoded String uri, @Context HttpHeaders httpHeaders, @Context UriInfo uriInfo,
      @Context HttpServletRequest req) {
    LoggingUtil.initMdcContext(req, httpHeaders);
    long startTimeInMs = System.currentTimeMillis();
    Response response = null;
    String urlContext = "/"+ uri;
    String queryParams = uriInfo.getRequestUri().getQuery();

    try {

      Map<String, List<String>> parameters = new HashMap<String, List<String>>();
      for (Map.Entry<String, List<String>> e : httpHeaders.getRequestHeaders().entrySet()) {
        parameters.put(e.getKey(), e.getValue());
      }
      if (uriInfo.getQueryParameters().containsKey("t-k")) {
        response = Response.status(Status.OK).entity(chameleonRouter.process(urlContext, queryParams, parameters))
            .build();
      } else {
        response = Response.status(Status.OK).entity(champRouter.process(urlContext, queryParams, parameters)).build();
      }
    } catch (DataRouterException ex) {
      response = Response.status(ex.getHttpStatus()).entity(ex.getMessage()).build();

    } catch (Exception e) {
      response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();

    } finally {      
      LoggingUtil.logRestRequest(logger, auditLogger, req, response);
      metricsLogger.info(DataRouterMsgs.PROCESSED_REQUEST, "GET",
          Long.toString(System.currentTimeMillis() - startTimeInMs));
    }
    return response;

  }

}

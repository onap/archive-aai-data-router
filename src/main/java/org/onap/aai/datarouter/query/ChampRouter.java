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
package org.onap.aai.datarouter.query;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.eclipse.jetty.util.security.Password;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.datarouter.exception.DataRouterException;
import org.onap.aai.datarouter.util.DataRouterConstants;
import org.onap.aai.restclient.client.OperationResult;
import org.onap.aai.restclient.client.RestClient;
import org.onap.aai.restclient.rest.HttpUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


@Component
@Qualifier("champ")
public class ChampRouter implements QueryRouter {
  Logger logger = LoggerFactory.getInstance().getLogger(ChampRouter.class.getName());

  private String champBaseURL;

  private RestClient restClient ;
  
  public ChampRouter(){}

  public ChampRouter(String champBaseURL, RestClientConfig config) {   
    this.champBaseURL = champBaseURL;
    this.restClient = new RestClient().validateServerHostname(false).validateServerCertChain(true)
        .clientCertFile(config.getCertPath())
        .clientCertPassword(Password.deobfuscate(config.getCertPassword()))
        .trustStore(config.getTrustStorePath())
        .connectTimeoutMs(config.getConnectionTimeout())
        .readTimeoutMs(config.getReadTimeout());
    validate();
  }

 
  public void validate(){
    String baseURL = champBaseURL.endsWith("/") ? champBaseURL.substring(0, champBaseURL.length() - 1) : champBaseURL;
    if (baseURL.contains(DATA_ROUTER_PORT)) {
      logger.info(QueryMsgs.QUERY_INFO, "Invalid champBaseURL : Can't re-route back to DataRouter " + champBaseURL);
      throw new InvalidParameterException("Invalid champBaseURL : Can't re-route back to DataRouter " + champBaseURL);
    }
    this.champBaseURL = baseURL;
  }

  @Override
  public String process(String urlContext, String queryParams, Map<String, List<String>> headers)
      throws DataRouterException {
    String champURL;
    String response;
    if (queryParams != null && !queryParams.isEmpty()) {
      champURL = champBaseURL + urlContext + "?" + queryParams;
    } else {
      champURL = champBaseURL + urlContext;
    }

    logger.info(QueryMsgs.QUERY_INFO, "Routing request to Champ service URL: " + champURL);

    headers = headers == null ? new HashMap<String, List<String>>() : headers;
    headers.put("X-FromAppId", Arrays.asList(DataRouterConstants.DATA_ROUTER_SERVICE_NAME));
    OperationResult result = restClient.get(champURL, headers, MediaType.APPLICATION_JSON_TYPE);

    if (HttpUtil.isHttpResponseClassSuccess(result.getResultCode())) {
      response = result.getResult();
    } else {
      logger.info(QueryMsgs.QUERY_ERROR,
          "Error while calling Champ service URL: " + champURL + " failure cause: " + result.getFailureCause());
      throw new DataRouterException(
          "Error while calling Champ service URL: " + champURL + " failure cause: " + result.getFailureCause(),
          Status.fromStatusCode(result.getResultCode()));
    }

    return response;

  }

}

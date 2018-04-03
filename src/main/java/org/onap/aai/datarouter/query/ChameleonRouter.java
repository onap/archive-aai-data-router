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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("chameleon")
public class ChameleonRouter implements QueryRouter {

  Logger logger = LoggerFactory.getInstance().getLogger(ChameleonRouter.class.getName());

  private String chameleonBaseURL;

  private RestClient restClient ;

  private enum ChameleonAction {
    GET_OBJECT_BY_ID, GET_REL_BY_ID, GET_OBJECT_RELS, GET_OBJECTS_BY_FILTER, GET_RELS_BY_FILTER
  };

  private static final Pattern QUERY_OBJECT_FILTER_URL_MATCH = Pattern.compile("/objects/filter(.*)");
  private static final Pattern QUERY_REL_FILTER_URL_MATCH = Pattern.compile("/relationships/filter(.*)");
  private static final Pattern QUERY_OBJECT_REL_URL_MATCH = Pattern.compile("/objects/relationships/(.*)");
  private static final Pattern QUERY_OBJECT_ID_URL_MATCH = Pattern.compile("/objects/(.*)");
  private static final Pattern QUERY_REL_ID_URL_MATCH = Pattern.compile("/relationships/(.*)");


  public ChameleonRouter(){}
  
  
  public ChameleonRouter(String chameleonBaseURL, RestClientConfig config) {   
    this.chameleonBaseURL = chameleonBaseURL;
    this.restClient = new RestClient().validateServerHostname(false).validateServerCertChain(true)
        .clientCertFile(config.getCertPath())
        .clientCertPassword(Password.deobfuscate(config.getCertPassword()))
        .trustStore(config.getTrustStorePath())
        .connectTimeoutMs(config.getConnectionTimeout())
        .readTimeoutMs(config.getReadTimeout());
    validate();
  }

  
  public void validate() {
    String baseURL = chameleonBaseURL.endsWith("/") ? chameleonBaseURL.substring(0, chameleonBaseURL.length() - 1)
        : chameleonBaseURL;
    if (baseURL.contains(DATA_ROUTER_PORT)) {
      logger.error(QueryMsgs.QUERY_ERROR,
          "Invalid chameleonBaseURL : Can't re-route back to DataRouter " + chameleonBaseURL);
      throw new InvalidParameterException(
          "Invalid chameleonBaseURL : Can't re-route back to DataRouter " + chameleonBaseURL);
    }
    this.chameleonBaseURL = baseURL;
  }

  private boolean urlMatcher(Pattern p, String url) {
    Matcher m = p.matcher(url);
    if (m.matches() && !m.group(1).contains("/")) {
      return true;
    } else {
      return false;
    }
  }

  private ChameleonAction resolveChameleonAction(String urlContext) throws DataRouterException {

    urlContext = urlContext.endsWith("/") ? urlContext.substring(0, urlContext.length() - 1) : urlContext;
    ChameleonAction action;

    if (urlMatcher(QUERY_OBJECT_FILTER_URL_MATCH, urlContext)) {
      action = ChameleonAction.GET_OBJECTS_BY_FILTER;
    } else if (urlMatcher(QUERY_REL_FILTER_URL_MATCH, urlContext)) {
      action = ChameleonAction.GET_RELS_BY_FILTER;
    } else if (urlMatcher(QUERY_OBJECT_REL_URL_MATCH, urlContext)) {
      action = ChameleonAction.GET_OBJECT_RELS;
    } else if (urlMatcher(QUERY_OBJECT_ID_URL_MATCH, urlContext)) {
      action = ChameleonAction.GET_OBJECT_BY_ID;
    } else if (urlMatcher(QUERY_REL_ID_URL_MATCH, urlContext)) {
      action = ChameleonAction.GET_REL_BY_ID;
    } else {

      throw new DataRouterException("", Status.NOT_FOUND);
    }
    return action;
  }

  private String buildUrl(String urlContext, String queryParams, ChameleonAction action) {

    urlContext = urlContext.endsWith("/") ? urlContext.substring(0, urlContext.length() - 1) : urlContext;
    String ecompUrl = "";
    String ID = "";

    switch (action) {
    case GET_OBJECT_BY_ID:
      ID = urlContext.substring(urlContext.lastIndexOf("/") + 1, urlContext.length());
      if (ID == null || ID.isEmpty()) {
        throw new IllegalArgumentException("Invalid URI path with no Object ID: " + urlContext);
      } else {
        if (queryParams != null && !queryParams.isEmpty()) {
          ecompUrl = chameleonBaseURL + "/" + ID + "?" + queryParams;

        } else {
          ecompUrl = chameleonBaseURL + "/" + ID;
        }
      }

      break;

    case GET_REL_BY_ID:
      ID = urlContext.substring(urlContext.lastIndexOf("/") + 1, urlContext.length());
      if (ID == null || ID.isEmpty()) {
        throw new IllegalArgumentException("Invalid URI path with no Relationship ID: " + urlContext);
      } else {
        if (queryParams != null && !queryParams.isEmpty()) {
          ecompUrl = chameleonBaseURL + "/" + ID + "?" + queryParams;

        } else {
          ecompUrl = chameleonBaseURL + "/" + ID;
        }
      }

      break;

    case GET_OBJECT_RELS:
      ID = urlContext.substring(urlContext.lastIndexOf("/") + 1, urlContext.length());
      if (ID == null || ID.isEmpty()) {
        throw new IllegalArgumentException("Invalid URI path with no Object ID: " + urlContext);
      } else {
        if (queryParams != null && !queryParams.isEmpty()) {
          // TODO: Fix the URL for getting object relations when Chameloen
          // supports this API
          ecompUrl = chameleonBaseURL + "/relations" + ID + "?" + queryParams;

        } else {
          ecompUrl = chameleonBaseURL + "/relations" + ID;
        }
      }

      break;

    case GET_OBJECTS_BY_FILTER:
      if (queryParams != null && !queryParams.isEmpty()) {
        // TODO: Fix the URL for getting object filter when Chameloen
        // supports this API
        ecompUrl = chameleonBaseURL + "/filter?" + queryParams;
      } else {
        ecompUrl = chameleonBaseURL + "/filter";
      }

      break;

    case GET_RELS_BY_FILTER:
      if (queryParams != null && !queryParams.isEmpty()) {
        // TODO: Fix the URL for getting rel filter when Chameloen
        // supports this API
        ecompUrl = chameleonBaseURL + "/filter?" + queryParams;
      } else {
        ecompUrl = chameleonBaseURL + "/filter";
      }

      break;

    }

    return ecompUrl;
  }

  private String parseResponse(String urlContext, OperationResult result, ChameleonAction action)
      throws DataRouterException {

    Integer httpResponseCode = result.getResultCode();
    String ID = urlContext.substring(urlContext.lastIndexOf("/") + 1, urlContext.length());

    switch (action) {
    case GET_OBJECT_BY_ID:
      if (httpResponseCode >= 200 && httpResponseCode <= 299) {
        if (ID == null || ID.isEmpty()) {
          throw new DataRouterException("", Status.BAD_REQUEST);
        } else {
          return ChameleonResponseBuiler.buildEntity(result.getResult(), ID);
        }
      } else {
        throw new DataRouterException("", Status.fromStatusCode(httpResponseCode));
      }

    case GET_REL_BY_ID:
      if (httpResponseCode >= 200 && httpResponseCode <= 299) {

        if (ID == null || ID.isEmpty()) {
          throw new DataRouterException("", Status.BAD_REQUEST);
        } else {
          return ChameleonResponseBuiler.buildEntity(result.getResult(), ID);
        }
      } else {
        throw new DataRouterException("", Status.fromStatusCode(httpResponseCode));
      }

    case GET_OBJECT_RELS:

      // TODO:Return 200 with empty body for now until chameleon supports this
      // query
      if (ID == null || ID.isEmpty()) {
        throw new DataRouterException("", Status.BAD_REQUEST);
      } else {
        return ChameleonResponseBuiler.buildObjectRelationship(result.getResult(), ID);
      }

    case GET_OBJECTS_BY_FILTER:
      // TODO:Return 200 with empty body for now until chameleon supports this
      // query
      return ChameleonResponseBuiler.buildCollection(result.getResult());

    case GET_RELS_BY_FILTER:
      // TODO:Return 200 with empty body for now until chameleon supports this
      // query
      return ChameleonResponseBuiler.buildCollection(result.getResult());
    default:
      throw new DataRouterException("", Status.NOT_FOUND);

    }

  }

  @Override
  public String process(String urlContext, String queryParams, Map<String, List<String>> headers)
      throws DataRouterException {
    String response;
    ChameleonAction action = resolveChameleonAction(urlContext);
    String chameleonURL = buildUrl(urlContext, queryParams, action);
    logger.info(QueryMsgs.QUERY_INFO, "Routing request to Chameleon service URL: " + chameleonURL);

    headers = headers == null ? new HashMap<String, List<String>>() : headers;
    headers.put("X-FromAppId", Arrays.asList(DataRouterConstants.DATA_ROUTER_SERVICE_NAME));
    OperationResult result = restClient.get(chameleonURL, headers, MediaType.APPLICATION_JSON_TYPE);

    try {
      response = parseResponse(urlContext, result, action);
    } catch (DataRouterException ex) {
      logger.info(QueryMsgs.QUERY_ERROR,
          "Error while calling Chameleon service URL: " + chameleonURL + " failure cause: " + result.getFailureCause());
      throw ex;
    }

    return response;
  }

}

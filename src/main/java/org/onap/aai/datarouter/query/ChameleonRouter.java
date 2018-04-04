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
package  org.onap.aai.datarouter.query;

import java.security.InvalidParameterException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.BadRequestException;

import org.apache.camel.Exchange;
import org.apache.camel.RuntimeCamelException;
import org.onap.aai.rest.RestClientEndpoint;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;

public class ChameleonRouter extends QueryRouter {

  Logger logger = LoggerFactory.getInstance().getLogger(ChameleonRouter.class.getName());

  private String chameleonBaseURL;

  private enum ChameleonAction {
    GET_OBJECT_BY_ID, GET_REL_BY_ID, GET_OBJECT_RELS, GET_OBJECTS_BY_FILTER, GET_RELS_BY_FILTER
  };

  private static final Pattern QUERY_OBJECT_FILTER_URL_MATCH = Pattern.compile("/objects/filter(.*)");
  private static final Pattern QUERY_REL_FILTER_URL_MATCH = Pattern.compile("/relationships/filter(.*)");
  private static final Pattern QUERY_OBJECT_REL_URL_MATCH = Pattern.compile("/objects/relationships/(.*)");
  private static final Pattern QUERY_OBJECT_ID_URL_MATCH = Pattern.compile("/objects/(.*)");
  private static final Pattern QUERY_REL_ID_URL_MATCH = Pattern.compile("/relationships/(.*)");

  private static final String ECOMP_QUERY_ID = "ECOMP_QUERY_ID";
  private static final String ECOMP_QUERY_TYPE = "ECOMP_QUERY_TYPE";

  public ChameleonRouter(String chameleonBaseURL) {
    String baseURL = chameleonBaseURL.endsWith("/") ? chameleonBaseURL.substring(0, chameleonBaseURL.length() - 1)
        : chameleonBaseURL;
    if (checkRecursion(baseURL)) {
      logger.error(QueryMsgs.QUERY_ERROR,
          "Invalid chameleonBaseURL : Can't re-route back to DataRouter " + chameleonBaseURL);
      throw new InvalidParameterException(
          "Invalid chameleonBaseURL : Can't re-route back to DataRouter " + chameleonBaseURL);
    }
    this.chameleonBaseURL = baseURL;
  }

  public void setQueryRequest(Exchange exchange) {
    setMDC(exchange);
    ChameleonAction action = resolveChameleonAction(exchange);
    String ecompUrl = buildUrl(exchange, action);
    logger.info(QueryMsgs.QUERY_INFO, "Routing request to Chameleon service URL: " + ecompUrl);
    exchange.getIn().setHeader(RestClientEndpoint.IN_HEADER_URL, ecompUrl);
    exchange.getIn().setHeader("X-FromAppId", SERVICE_NAME);
    exchange.getIn().setHeader("X-TransactionId", getTxId(exchange));

  }

  private boolean urlMatcher(Pattern p, String url) {
    Matcher m = p.matcher(url);
    if (m.matches() && !m.group(1).contains("/")) {
      return true;
    } else {
      return false;
    }
  }

  private ChameleonAction resolveChameleonAction(Exchange exchange) {
    String path = exchange.getIn().getHeader(Exchange.HTTP_PATH, String.class);
    path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    ChameleonAction action;

    if (urlMatcher(QUERY_OBJECT_FILTER_URL_MATCH, path)) {
      action = ChameleonAction.GET_OBJECTS_BY_FILTER;
    } else if (urlMatcher(QUERY_REL_FILTER_URL_MATCH, path)) {
      action = ChameleonAction.GET_RELS_BY_FILTER;
    } else if (urlMatcher(QUERY_OBJECT_REL_URL_MATCH, path)) {
      action = ChameleonAction.GET_OBJECT_RELS;
    } else if (urlMatcher(QUERY_OBJECT_ID_URL_MATCH, path)) {
      action = ChameleonAction.GET_OBJECT_BY_ID;
    } else if (urlMatcher(QUERY_REL_ID_URL_MATCH, path)) {
      action = ChameleonAction.GET_REL_BY_ID;
    } else {
      exchange.getIn().setHeader(ChameleonErrorProcessor.ECOMP_QUERY_ERROR_CODE, 404);
      throw new RuntimeCamelException();
    }
    return action;
  }

  private String buildUrl(Exchange exchange, ChameleonAction action) {
    String path = exchange.getIn().getHeader(Exchange.HTTP_PATH, String.class);
    path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    String queryParams = exchange.getIn().getHeader(Exchange.HTTP_QUERY, String.class);
    String ecompUrl = "";
    String ID = "";

    switch (action) {
    case GET_OBJECT_BY_ID:
      ID = path.substring(path.lastIndexOf("/") + 1, path.length());
      if (ID == null || ID.isEmpty()) {
        throw new IllegalArgumentException("Invalid URI path with no Object ID: " + path);
      } else {
        if (queryParams != null && !queryParams.isEmpty()) {
          ecompUrl = chameleonBaseURL + "/" + ID + "?" + queryParams;

        } else {
          ecompUrl = chameleonBaseURL + "/" + ID;
        }
      }
      exchange.getIn().setHeader(ECOMP_QUERY_ID, ID);
      exchange.getIn().setHeader(ECOMP_QUERY_TYPE, ChameleonAction.GET_OBJECT_BY_ID);
      break;

    case GET_REL_BY_ID:
      ID = path.substring(path.lastIndexOf("/") + 1, path.length());
      if (ID == null || ID.isEmpty()) {
        throw new IllegalArgumentException("Invalid URI path with no Relationship ID: " + path);
      } else {
        if (queryParams != null && !queryParams.isEmpty()) {
          ecompUrl = chameleonBaseURL + "/" + ID + "?" + queryParams;

        } else {
          ecompUrl = chameleonBaseURL + "/" + ID;
        }
      }
      exchange.getIn().setHeader(ECOMP_QUERY_ID, ID);
      exchange.getIn().setHeader(ECOMP_QUERY_TYPE, ChameleonAction.GET_REL_BY_ID);
      break;

    case GET_OBJECT_RELS:
      ID = path.substring(path.lastIndexOf("/") + 1, path.length());
      if (ID == null || ID.isEmpty()) {
        throw new IllegalArgumentException("Invalid URI path with no Object ID: " + path);
      } else {
        if (queryParams != null && !queryParams.isEmpty()) {
          // TODO: Fix the URL for getting object relations when Chameloen
          // supports this API
          ecompUrl = chameleonBaseURL + "/relations" + ID + "?" + queryParams;

        } else {
          ecompUrl = chameleonBaseURL + "/relations" + ID;
        }
      }
      exchange.getIn().setHeader(ECOMP_QUERY_ID, ID);
      exchange.getIn().setHeader(ECOMP_QUERY_TYPE, ChameleonAction.GET_OBJECT_RELS);
      break;

    case GET_OBJECTS_BY_FILTER:
      if (queryParams != null && !queryParams.isEmpty()) {
        // TODO: Fix the URL for getting object filter when Chameloen
        // supports this API
        ecompUrl = chameleonBaseURL + "/filter?" + queryParams;
      } else {
        ecompUrl = chameleonBaseURL + "/filter";
      }
      exchange.getIn().setHeader(ECOMP_QUERY_TYPE, ChameleonAction.GET_OBJECTS_BY_FILTER);
      break;

    case GET_RELS_BY_FILTER:
      if (queryParams != null && !queryParams.isEmpty()) {
        // TODO: Fix the URL for getting rel filter when Chameloen
        // supports this API
        ecompUrl = chameleonBaseURL + "/filter?" + queryParams;
      } else {
        ecompUrl = chameleonBaseURL + "/filter";
      }
      exchange.getIn().setHeader(ECOMP_QUERY_TYPE, ChameleonAction.GET_RELS_BY_FILTER);
      break;

    }

    return ecompUrl;
  }

  public void setQueryResponse(Exchange exchange) {
    parseResponse(exchange);
    adjustHeaders(exchange);
  }

  private void adjustHeaders(Exchange exchange) {
    // Remove the internal heders
    exchange.getIn().removeHeader(ECOMP_QUERY_ID);
    exchange.getIn().removeHeader(ECOMP_QUERY_TYPE);
  }

  private void parseResponse(Exchange exchange) throws BadRequestException {

    ChameleonAction action = exchange.getIn().getHeader(ECOMP_QUERY_TYPE, ChameleonAction.class);
    Integer httpResponseCode = exchange.getIn().getHeader(RestClientEndpoint.OUT_HEADER_RESPONSE_CODE, Integer.class);
    String ID = "";

    switch (action) {
    case GET_OBJECT_BY_ID:
      if (httpResponseCode >= 200 && httpResponseCode <= 299) {
        ID = exchange.getIn().getHeader(ECOMP_QUERY_ID, String.class);
        if (ID == null || ID.isEmpty()) {
          exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
        } else {
          ChameleonResponseBuiler.buildEntity(exchange, ID);
        }
      } else {
        exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, httpResponseCode);
      }
      break;
    case GET_REL_BY_ID:
      if (httpResponseCode >= 200 && httpResponseCode <= 299) {
        ID = exchange.getIn().getHeader(ECOMP_QUERY_ID, String.class);
        if (ID == null || ID.isEmpty()) {
          exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
        } else {
          ChameleonResponseBuiler.buildEntity(exchange, ID);
        }
      } else {
        exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, httpResponseCode);
      }
      break;
    case GET_OBJECT_RELS:
      if (httpResponseCode >= 200 && httpResponseCode <= 299) {
        ID = exchange.getIn().getHeader(ECOMP_QUERY_ID, String.class);
        if (ID == null || ID.isEmpty()) {
          exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
        } else {
          ChameleonResponseBuiler.buildObjectRelationship(exchange, ID);
        }
      } else {
        // TODO:Return 200 with empty body for now until chameleon supports this
        // query
        exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
        exchange.getIn().setBody("[]");
      }
      break;
    case GET_OBJECTS_BY_FILTER:
      if (httpResponseCode >= 200 && httpResponseCode <= 299) {
        ChameleonResponseBuiler.buildCollection(exchange);
      } else {
        // TODO:Return 200 with empty body for now until chameleon supports this
        // query
        exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
        exchange.getIn().setBody("[]");
      }
      break;
    case GET_RELS_BY_FILTER:
      if (httpResponseCode >= 200 && httpResponseCode <= 299) {
        ChameleonResponseBuiler.buildCollection(exchange);
      } else {
        // TODO:Return 200 with empty body for now until chameleon supports this
        // query
        exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
        exchange.getIn().setBody("[]");
      }
      break;

    }

  }

}

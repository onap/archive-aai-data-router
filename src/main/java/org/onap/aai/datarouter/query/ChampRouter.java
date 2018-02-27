/**
 * ﻿============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2017 Amdocs
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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */
package org.onap.aai.datarouter.query;

import java.security.InvalidParameterException;

import org.apache.camel.Exchange;
import org.onap.aai.rest.RestClientEndpoint;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;

public class ChampRouter extends QueryRouter {
  Logger logger = LoggerFactory.getInstance().getLogger(ChampRouter.class.getName());

  private String champBaseURL;
  

  public ChampRouter(String champBaseURL) {
    String baseURL = champBaseURL.endsWith("/") ? champBaseURL.substring(0, champBaseURL.length() - 1)
        : champBaseURL;
    if(checkRecursion(baseURL)){
      logger.info(QueryMsgs.QUERY_INFO, "Invalid champBaseURL : Can't re-route back to DataRouter "+ champBaseURL);
      throw new InvalidParameterException("Invalid champBaseURL : Can't re-route back to DataRouter "+ champBaseURL);
    }
    this.champBaseURL = baseURL;    
  }

  public void setQueryRequest(Exchange exchange) {
    setMDC(exchange);
    String path = exchange.getIn().getHeader(Exchange.HTTP_PATH, String.class);
    String queryParams = exchange.getIn().getHeader(Exchange.HTTP_QUERY, String.class);
    String ecompUrl;
    if (queryParams != null && !queryParams.isEmpty()) {
      ecompUrl = champBaseURL + path + "?" + queryParams;
    } else {
      ecompUrl = champBaseURL + path;
    }

    logger.info(QueryMsgs.QUERY_INFO, "Routing request to Champ service URL: " + ecompUrl);
    exchange.getIn().setHeader(RestClientEndpoint.IN_HEADER_URL, ecompUrl);
    exchange.getIn().setHeader("X-FromAppId", SERVICE_NAME);
    exchange.getIn().setHeader("X-TransactionId", getTxId(exchange));

  }

  public void setQueryResponse(Exchange exchange) {
    Integer httpResponseCode = exchange.getIn().getHeader(RestClientEndpoint.OUT_HEADER_RESPONSE_CODE, Integer.class);
    // Object httpBody = exchange.getIn().getBody();
    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, httpResponseCode);

  }

}

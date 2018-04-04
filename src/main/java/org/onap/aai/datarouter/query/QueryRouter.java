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

import java.util.Map;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.cl.mdc.MdcContext;


public abstract class QueryRouter {
  Logger logger = LoggerFactory.getInstance().getLogger(QueryRouter.class.getName());


  protected static final String SERVICE_NAME = "DATA-ROUTER";
  /** HTTP header containing the ECOMP Request Id */
  protected static final String HEADER_TRANS_ID = "X-TransactionId";
  
  /** HTTP header containing the calling application Id */
  protected static final String HEADER_FROM_APP_ID = "X-FromAppId";
  
  /** HTTP header containing the calling host details */
  protected static final String HEADER_FROM_HOST = "Host";
  
  /** HTTP header containing the calling host details */
  protected static final String DATA_ROUTER_PORT = "9502";
  

  public abstract void setQueryRequest(Exchange exchange) ;
  
  public abstract void setQueryResponse(Exchange exchange);
  
  protected String getTxId(final Exchange exchange){
    String txId = exchange.getIn().getHeader("X-TransactionId",String.class);    
    return ((txId==null||txId.isEmpty())?UUID.randomUUID().toString():txId);
  }
  
  protected boolean checkRecursion(String url){
   return url.contains(DATA_ROUTER_PORT);
  }
  
  protected void setMDC(final Exchange exchange) {
    String transId = exchange.getIn().getHeader(HEADER_TRANS_ID, String.class);
    String appId = exchange.getIn().getHeader(HEADER_FROM_APP_ID, String.class);
    String hostId = exchange.getIn().getHeader(HEADER_FROM_HOST, String.class);

    // Set MDC transaction id, calling service name, calling service id,
    // partner name, client address
    MdcContext.initialize(transId, "DataRouter", "", appId, hostId);
  }
 

}

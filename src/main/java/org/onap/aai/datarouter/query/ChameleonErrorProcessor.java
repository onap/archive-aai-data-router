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

import org.apache.camel.Exchange;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;

public class ChameleonErrorProcessor  {
  
  Logger logger = LoggerFactory.getInstance().getLogger(ChameleonRouter.class.getName());
  
  public static final String ECOMP_QUERY_ERROR_CODE = "ECOMP_QUERY_ERROR_CODE";

  private ChameleonErrorProcessor(){}
  
  
  public void process(Exchange exchange) throws Exception {
    int code = 500;
    if (exchange.getIn().getHeader(ECOMP_QUERY_ERROR_CODE, Integer.class) != null) {
      code = exchange.getIn().getHeader(ECOMP_QUERY_ERROR_CODE, Integer.class);
    }
    Exception e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
    if (e != null) {
      logger.error(QueryMsgs.QUERY_ERROR, e.toString());
    }

    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, code);
  }
}

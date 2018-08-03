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
package org.onap.aai.datarouter.util;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.onap.aai.cl.api.LogFields;
import org.onap.aai.cl.api.LogLine;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.mdc.MdcContext;
import org.onap.aai.datarouter.exception.POAAuditException;
import org.onap.aai.datarouter.logging.DataRouterMsgs;
import org.onap.aai.datarouter.util.DataRouterConstants;
import org.slf4j.MDC;

public class LoggingUtil {

	private static String UNKNOWN = "unknown";

	/**
	 * Initializes MDC context.
	 * Called when request processing begins.
	 * @param httpReq
	 * @param headers
	 */
	public static void initMdc(String transactionId, String fromAppId, String remoteAddr) {
		MdcContext.initialize(transactionId, DataRouterConstants.DATA_ROUTER_SERVICE_NAME, "", fromAppId, remoteAddr);
	}

	/**
	 * Clears the MDC context.
	 * Called when request processing ends.
	 */
	public static void closeMdc() {
		MDC.clear();
	}


	/**
	 * Generates error and audit logs
	 */
	public static void logRestRequest(Logger logger, Logger auditLogger, HttpServletRequest req, Response response) {
		logRestRequest(logger, auditLogger, req, response, null);
	}


	/**
	 * Generates error and audit logs
	 * @param logger
	 * @param auditLogger
	 * @param req
	 * @param response
	 * @param exception
	 */
	public static void logRestRequest(Logger logger, Logger auditLogger, HttpServletRequest req, Response response, POAAuditException exception) {

		String respStatusString = UNKNOWN;
		if(Response.Status.fromStatusCode(response.getStatus()) != null) {
			respStatusString = Response.Status.fromStatusCode(response.getStatus()).toString();
		}

		LogFields logFields = new LogFields().setField(LogLine.DefinedFields.RESPONSE_CODE, response.getStatus())
				.setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION, respStatusString);

		if((exception != null) && (exception.getLogCode() != null)) {
			logger.error(exception.getLogCode(), exception.getLogArguments());
			auditLogger.error(exception.getLogCode(), logFields, exception.getLogArguments());
		}

		String status = Integer.toString(response.getStatus());
		String method = (req != null) ? req.getMethod() : UNKNOWN;
		String reqUrl = (req != null) ? req.getRequestURL().toString() : UNKNOWN;
		String remoteHost = (req != null) ? req.getRemoteHost() : UNKNOWN;

		logger.info(DataRouterMsgs.PROCESS_REST_REQUEST, method, reqUrl, remoteHost, status);
		auditLogger.info(DataRouterMsgs.PROCESS_REST_REQUEST, logFields, method, reqUrl, remoteHost, status);
	}
}

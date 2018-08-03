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
package org.onap.aai.datarouter.exception;

import javax.ws.rs.core.Response.Status;
import org.onap.aai.datarouter.logging.DataRouterMsgs;

/**
 * This class is to handle the POMBA specific exception
 *
 */
public class POAAuditException extends Exception {

	private static final long serialVersionUID = 8162385108397238865L;

	private Status httpStatus;
	private DataRouterMsgs logCode;
	private String[] logArguments;

	public POAAuditException(String messageForResponse, Status httpStatus) {
		super(messageForResponse);
		this.setHttpStatus(httpStatus);
	}

	public POAAuditException(String message, Status httpStatus, Throwable cause) {
		super(message, cause);
		this.setHttpStatus(httpStatus);
	}

	public POAAuditException(Throwable cause) {
		super(cause);
	}

	public POAAuditException(String message, Throwable cause) {
		super(message, cause);
	}

	public POAAuditException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public POAAuditException(String message, Status httpStatus, DataRouterMsgs logCode, String... logArgs) {
		super(message);
		this.setHttpStatus(httpStatus);
		this.logCode = logCode;
		logArguments = new String[logArgs.length];
		int i = 0;
		for(String arg : logArgs) {
			logArguments[i++] = arg;
		}
	}

	public Status getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(Status httpStatus) {
		this.httpStatus = httpStatus;
	}

	public DataRouterMsgs getLogCode() {
		return logCode;
	}

	public String[] getLogArguments() {
		return logArguments;
	}
}

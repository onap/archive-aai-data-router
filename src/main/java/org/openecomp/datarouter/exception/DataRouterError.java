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
package org.openecomp.datarouter.exception;

/*
 * COPYRIGHT NOTICE: Copyright (c) 2016 Team Pacifica (Amdocs & AT&T) The contents and intellectual
 * property contained herein, remain the property of Team Pacifica (Amdocs & AT&T).
 */

import java.text.MessageFormat;

import javax.ws.rs.core.Response.Status;

/**
 * DL enum for error conditions.
 */
public enum DataRouterError {

  /** Parsing exceptions - Range 100..199. */
  DL_PARSE_100("DL-100", "Unable to find resource {0} in the model", Status.BAD_REQUEST), 
  DL_PARSE_101("DL-101", "Unable to parse ", Status.BAD_REQUEST), 
  DL_PARSE_102("DL-102", "Sot Filter error: {0} ", Status.INTERNAL_SERVER_ERROR), 
  DL_PARSE_103("DL-103", "URL Parsing error: {0} ", Status.BAD_REQUEST), 
  DL_PARSE_104("DL-104", "Missing Ids filter: {0} ", Status.BAD_REQUEST), 
  DL_PARSE_105("DL-105", "Invalid Ids filter: {0} ", Status.BAD_REQUEST),

  /** Validation exceptions - Range 200..299. */
  DL_VALIDATION_200("DL-200", "Missing X-TransactionId in header ", Status.BAD_REQUEST),

  /** Other components integration errors - Range 300..399. */
  DL_INTEGRATION_300("DL-300", "Unable to decorate Graph ", Status.INTERNAL_SERVER_ERROR),

  /** Environment related exceptions - Range 400..499. */
  DL_ENV_400("DL-400", "Unable to find file {0} ", Status.INTERNAL_SERVER_ERROR), 
  DL_ENV_401("DL-401", "Unable to Load OXM Models", Status.INTERNAL_SERVER_ERROR),

  /** Other components integration errors - Range 500..599. */
  DL_AUTH_500("DL-500", "Unable to authorize User ", Status.FORBIDDEN);

  /** The error id. */
  private String id;
  /** The error message. */
  private String message;
  /** The error http return code. */
  private Status status;

  /**
   * Constructor.
   * 
   * @param id the error id
   * @param message the error message
   */
  DataRouterError(final String id, final String message, final Status status) {
    this.id = id;
    this.message = message;
    this.status = status;
  }

  /**
   * Get the id.
   * 
   * @return the error id
   */
  public String getId() {
    return this.id;
  }

  /**
   * Get the message.
   * 
   * @param args the error arguments
   * @return the error message
   */
  public String getMessage(final Object... args) {
    final MessageFormat formatter = new MessageFormat("");
    formatter.applyPattern(this.message);
    return formatter.format(args);
  }

  public Status getHttpStatus() {
    return this.status;
  }

}

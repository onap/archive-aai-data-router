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

/*
 * COPYRIGHT NOTICE: Copyright (c) 2016 Team Pacifica (Amdocs & AT&T) The contents and intellectual
 * property contained herein, remain the property of Team Pacifica (Amdocs & AT&T).
 */

import java.util.Locale;

/**
 * Base SMAdaptor exception class.
 */
public class BaseDataRouterException extends Exception {

  /** Force serialVersionUID. */
  private static final long serialVersionUID = -6663403070792969748L;

  /** Default locale. */
  public static final Locale LOCALE = Locale.US;

  /** Exception id. */
  private final String id;

  /**
   * Constructor.
   * 
   * @param id the incoming id.
   */
  public BaseDataRouterException(final String id) {
    super();
    this.id = id;
  }

  /**
   * Constructor.
   * 
   * @param id the incoming id
   * @param message the incoming message
   */
  public BaseDataRouterException(final String id, final String message) {
    super(message);
    this.id = id;
  }

  /**
   * Constructor.
   * 
   * @param id the incoming id
   * @param message the incoming message
   * @param cause the incoming throwable
   */
  public BaseDataRouterException(final String id, final String message, final Throwable cause) {
    super(message, cause);
    this.id = id;
  }

  /**
   * Get the exception id.
   * 
   * @return the exception id
   */
  public String getId() {
    return this.id;
  }
}

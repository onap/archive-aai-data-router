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

import org.onap.aai.cl.eelf.LogMessageEnum;

import com.att.eelf.i18n.EELFResourceManager;

public enum QueryMsgs implements LogMessageEnum {

	/**
	   * Arguments: {0} = Enricher URL
	   */
	  QUERY_INFO,

    QUERY_ERROR;
	
	 
	
	
    /**
     * Static initializer to ensure the resource bundles for this class are loaded...
     */
    static {
        EELFResourceManager.loadMessageBundle("logging/QueryMsgs");
    };
}

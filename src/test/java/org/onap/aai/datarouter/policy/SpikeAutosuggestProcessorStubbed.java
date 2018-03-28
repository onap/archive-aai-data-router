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
package org.onap.aai.datarouter.policy;

import java.io.FileNotFoundException;

import org.onap.aai.datarouter.entity.DocumentStoreDataEntity;

public class SpikeAutosuggestProcessorStubbed extends SpikeAutosuggestIndexProcessor {
	

	public SpikeAutosuggestProcessorStubbed(SpikeEventPolicyConfig config) throws FileNotFoundException {
		super(config);
		
	}

	protected void handleSearchServiceOperation(DocumentStoreDataEntity eventEntity, String action, String index) {
		//Stub out the actual call to Search Data service and instead store/update documents in memory
		try {
			switch (action.toLowerCase()) { 
			case "create":
				InMemorySearchDatastore.put(eventEntity.getId(), eventEntity.getAsJson()); // they are executed if variable == c1
				break;
			case "update":
				InMemorySearchDatastore.put(eventEntity.getId(), eventEntity.getAsJson()); // they are executed if variable == c1
				break;
			case "delete":
				InMemorySearchDatastore.remove(eventEntity.getId()); // they are executed if variable == c1
				break;
			default:
				break;
			}
		} catch (Exception ex) {
		}
	}
}

/**
 * ============LICENSE_START=======================================================
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


public class ServiceIntegrityValidationPolicyStubbed extends ServiceIntegrityValidationPolicy{

    private InMemorySearchDatastore searchDb;

    public ServiceIntegrityValidationPolicyStubbed(String searchCertPath, String searchCertTruststore,
            String searchCertPassword, String searchBaseURL, String endpoint, String validationIndexName,
            String violationIndexName) {
        super(searchCertPath, searchCertTruststore, searchCertPassword, searchBaseURL, endpoint, validationIndexName,
                violationIndexName);

    }


    public InMemorySearchDatastore getSearchDb() {
        return searchDb;
    }


    public ServiceIntegrityValidationPolicyStubbed withSearchDb(InMemorySearchDatastore searchDb) {
        this.searchDb = searchDb;
        return this;
    }


    public void handleSearchDataServiceOperation(String index, String id, String payload, String action) {

        //Stub out the actual call to Search Data service and instead store/update documents in memory
        try {
            switch (action.toLowerCase()) {
            case "put":
                searchDb.put(index, payload); // validation message
                break;
            case "post":
                searchDb.put(index, payload); // violation message
                break;

            default:
                break;
            }
        } catch (Exception ex) {
        }

    }
}

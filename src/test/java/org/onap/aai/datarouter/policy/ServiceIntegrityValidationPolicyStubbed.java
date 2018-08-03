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

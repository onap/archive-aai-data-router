package org.openecomp.datarouter.policy;

import java.io.FileNotFoundException;

import org.onap.aai.datarouter.entity.DocumentStoreDataEntity;

public class EntityEventPolicyStubbed extends EntityEventPolicy {
	

	public EntityEventPolicyStubbed(EntityEventPolicyConfig config) throws FileNotFoundException {
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

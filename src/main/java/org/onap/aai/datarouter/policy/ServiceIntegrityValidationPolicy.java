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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.cl.mdc.MdcContext;
import org.onap.aai.restclient.client.Headers;
import org.onap.aai.datarouter.logging.ServiceIntegrityValidationsMsgs;
import org.onap.aai.datarouter.util.SearchServiceAgent;
import org.slf4j.MDC;

/**
 *  This class handles the logic which transformers the POA-AUDIT-RESULT event message to the ElasticSearch validation/violation message
 *  in order to prepare two Json structures, one for validation index and one for violation index and then submit POST request to
 *  Search-Data-Service to insert the document into ES.
 *
 */
public class ServiceIntegrityValidationPolicy {

	private static final String SERVICE_VALIDATION_SCHEMA_FILE = "auditservice_validation_schema.json";
	private static final String SERVICE_VIOLATION_SCHEMA_FILE = "auditservice_violation_schema.json";

	private static Logger logger = LoggerFactory.getInstance().getLogger(ServiceIntegrityValidationPolicy.class.getSimpleName());

	private static JsonParser jsonParser = new JsonParser();

	private String validationIndexName = null;
	private String violationIndexName = null;

	private SearchServiceAgent searchAgent = null;

	public ServiceIntegrityValidationPolicy(String searchCertPath,
			String searchCertTruststore,
			String searchCertPassword,
			String searchBaseURL,
			String endpoint,
			String validationIndexName,
			String violationIndexName) {
		searchAgent = new SearchServiceAgent(searchCertPath,
				searchCertTruststore,
				searchCertPassword,
				concatSubURI(searchBaseURL, endpoint),
				"documents",
				logger);

		this.validationIndexName = validationIndexName;
		this.violationIndexName = violationIndexName;
	}

	public void startup(){

		searchAgent.createSearchIndex(validationIndexName, SERVICE_VALIDATION_SCHEMA_FILE);
		searchAgent.createSearchIndex(violationIndexName, SERVICE_VIOLATION_SCHEMA_FILE);

		logger.info(ServiceIntegrityValidationsMsgs.SI_POLICY_REGISTRATION);
	}

	public void process(Exchange exchange) throws Exception {

		logger.debug("Invoking ServiceIntegrityViolationPolicy with payload" + exchange.getIn().getBody());

		String payload = (String)exchange.getIn().getBody();

		JsonObject serviceValidation = jsonParser.parse(payload).getAsJsonObject();
		JsonElement serviceViolationsElement = serviceValidation.get("violations");
		// Calculate the document id to use for this entity.
		JsonElement id = serviceValidation.get("validationId");

		if(serviceViolationsElement == null || serviceViolationsElement.getAsJsonArray().size() == 0) {
			serviceValidation.addProperty("result", "Pass");
			logger.debug("Service integrity validation event processing for event with ID " + id + " as a Pass due to no violations.");
		}else {
			serviceValidation.addProperty("result", "Fail");
			logger.debug("Service integrity validation event processing for event with ID " + id + " as a Fail due to one or more violations.");
		}

		if(serviceViolationsElement != null) {
			JsonArray violationsArray = serviceViolationsElement.getAsJsonArray();
			Iterator<JsonElement> vit = violationsArray.iterator();
			while(vit.hasNext()) {
				JsonObject currentViolation = vit.next().getAsJsonObject();
				JsonObject oldViolationDetails =  currentViolation.getAsJsonObject("violationDetails");
				JsonObject newViolationDetails = new JsonObject();
				for (Map.Entry<String, JsonElement> e : oldViolationDetails.entrySet()) {
					String oldKey  = e.getKey();
					String newKey = oldKey.replace(".","-");
					newViolationDetails.add(newKey,e.getValue());
				}
				currentViolation.remove("violationDetails");
				currentViolation.remove("modelName");
				currentViolation.add("violationDetails",newViolationDetails);
				logger.debug("new violation:" + currentViolation.toString());
				JsonObject formattedViolation = buildViolation(serviceValidation, currentViolation);

				handleSearchDataServiceOperation(violationIndexName, null, formattedViolation.toString(), "POST");

			}
		}
		logger.debug("validation: " + serviceValidation.toString());

		JsonObject formattedValidation = buildValidation(serviceValidation);
		// Persist the entity that we received from the event to the Search Service.
		handleSearchDataServiceOperation(validationIndexName, id.getAsString(), formattedValidation.toString(), "PUT");

	}


	private JsonObject buildViolation(JsonObject validation, JsonObject violation) {
		JsonObject formattedViolation = new JsonObject();

		formattedViolation.addProperty("validationId", validation.get("validationId").getAsString());
		formattedViolation.addProperty("validationTimestamp", validation.get("validationTimestamp").getAsString());
		formattedViolation.addProperty("modelVersionId", validation.get("entity").getAsJsonObject().get("poa-event").getAsJsonObject().get("modelVersionId").getAsString());
		formattedViolation.addProperty("modelInvariantId", validation.get("entity").getAsJsonObject().get("poa-event").getAsJsonObject().get("modelInvariantId").getAsString());
		formattedViolation.addProperty("serviceInstanceId",validation.get("entity").getAsJsonObject().get("poa-event").getAsJsonObject().get("serviceInstanceId").getAsString());

		formattedViolation.addProperty("violationId", violation.get("violationId").getAsString());
		formattedViolation.addProperty("violationTimestamp", validation.get("validationTimestamp").getAsString());
		formattedViolation.addProperty("category", violation.get("category").getAsString());
		formattedViolation.addProperty("severity", violation.get("severity").getAsString());
		formattedViolation.addProperty("violationType", violation.get("violationType").getAsString());
		formattedViolation.addProperty("validationRule", violation.get("validationRule").getAsString());
		formattedViolation.addProperty("message", violation.get("errorMessage").getAsString());

		//formattedViolation.add("violationDetails",violation.get("violationDetails"));
		return formattedViolation;
	}

	private JsonObject buildValidation(JsonObject validation) {
		JsonObject formattedValidation = new JsonObject();

		formattedValidation.add("violations", validation.get("violations"));
		formattedValidation.addProperty("validationId", validation.get("validationId").getAsString());
		formattedValidation.addProperty("validationTimestamp", validation.get("validationTimestamp").getAsString());
		formattedValidation.addProperty("modelVersionId", validation.get("entity").getAsJsonObject().get("poa-event").getAsJsonObject().get("modelVersionId").getAsString());
		formattedValidation.addProperty("modelInvariantId", validation.get("entity").getAsJsonObject().get("poa-event").getAsJsonObject().get("modelInvariantId").getAsString());
		formattedValidation.addProperty("serviceInstanceId",validation.get("entity").getAsJsonObject().get("poa-event").getAsJsonObject().get("serviceInstanceId").getAsString());

		try {
			formattedValidation.addProperty("modelName", validation.get("entity").getAsJsonObject().get("context-list").getAsJsonObject().get("sdc").getAsJsonObject().get("service").getAsJsonObject().get("name").getAsString());
			logger.debug("model name: " +  validation.get("entity").getAsJsonObject().get("context-list").getAsJsonObject().get("sdc").getAsJsonObject().get("service").getAsJsonObject().get("name").getAsString());
		}catch (NullPointerException ex){
			ex.printStackTrace();
			formattedValidation.addProperty("modelName","");
		}

		return formattedValidation;
	}


	private static String concatSubURI(String... suburis) {
		String finalURI = "";

		for (String suburi : suburis) {

			if (suburi != null) {
				// Remove any leading / since we only want to append /
				suburi = suburi.replaceFirst("^/*", "");

				// Add a trailing / if one isn't already there
				finalURI += suburi.endsWith("/") ? suburi : suburi + "/";
			}
		}

		return finalURI;
	}


	public void handleSearchDataServiceOperation(String index, String id, String payload, String action) {

		Map<String, List<String>> headers = new HashMap<>();
		headers.put(Headers.FROM_APP_ID, Arrays.asList("DataRouter"));
		headers.put(Headers.TRANSACTION_ID, Arrays.asList(MDC.get(MdcContext.MDC_REQUEST_ID)));

		if (action.equalsIgnoreCase("PUT")) {
			searchAgent.putDocument(index, id, payload, headers);

		}else if (action.equalsIgnoreCase("POST")) {
			searchAgent.postDocument(index,  payload, headers);
		}

	}
}

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
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.cl.mdc.MdcContext;
import org.onap.aai.datarouter.entity.SpikeEventEntity;
import org.onap.aai.datarouter.entity.DocumentStoreDataEntity;
import org.onap.aai.datarouter.entity.SpikeEventVertex;
import org.onap.aai.datarouter.entity.OxmEntityDescriptor;
import org.onap.aai.datarouter.logging.EntityEventPolicyMsgs;
import org.onap.aai.datarouter.util.EntityOxmReferenceHelper;
import org.onap.aai.datarouter.util.ExternalOxmModelProcessor;
import org.onap.aai.datarouter.util.OxmModelLoader;
import org.onap.aai.datarouter.util.RouterServiceUtil;
import org.onap.aai.datarouter.util.SearchServiceAgent;
import org.onap.aai.restclient.client.Headers;
import org.onap.aai.restclient.client.OperationResult;
import org.onap.aai.restclient.rest.HttpUtil;
import org.slf4j.MDC;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class SpikeEntityEventPolicy implements Processor {

  public static final String additionalInfo = "Response of SpikeEntityEventPolicy";
  private static final String entitySearchSchema = "entitysearch_schema.json";

  private Collection<ExternalOxmModelProcessor> externalOxmModelProcessors;


  private final String ACTION_CREATE = "create";
  private final String EVENT_VERTEX = "vertex";
  private final static String ACTION_DELETE = "delete";
  private final String ACTION_UPDATE = "update";
  private final String PROCESS_SPIKE_EVENT = "Process Spike Event";
  private final String OPERATION_KEY = "operation";


  private final List<String> SUPPORTED_ACTIONS =
      Arrays.asList(ACTION_CREATE, ACTION_UPDATE, ACTION_DELETE);

  Map<String, DynamicJAXBContext> oxmVersionContextMap = new HashMap<>();
  private String oxmVersion = null;

  /** Agent for communicating with the Search Service. */
  private SearchServiceAgent searchAgent = null;
  private String entitySearchIndex;
  private String srcDomain;

  private Logger logger;
  private Logger metricsLogger;

  public enum ResponseType {
    SUCCESS, PARTIAL_SUCCESS, FAILURE;
  };

  public SpikeEntityEventPolicy(SpikeEntityEventPolicyConfig config) throws FileNotFoundException {
    LoggerFactory loggerFactoryInstance = LoggerFactory.getInstance();
    logger = loggerFactoryInstance.getLogger(SpikeEntityEventPolicy.class.getName());
    metricsLogger = loggerFactoryInstance.getMetricsLogger(SpikeEntityEventPolicy.class.getName());


    srcDomain = config.getSourceDomain();

    // Populate the index names.
    entitySearchIndex = config.getSearchEntitySearchIndex();

    // Instantiate the agent that we will use for interacting with the Search Service.
    searchAgent = new SearchServiceAgent(config.getSearchCertName(), config.getSearchKeystore(),
        config.getSearchKeystorePwd(),
        EntityEventPolicy.concatSubUri(config.getSearchBaseUrl(), config.getSearchEndpoint()),
        config.getSearchEndpointDocuments(), logger);

    this.externalOxmModelProcessors = new ArrayList<>();
    this.externalOxmModelProcessors.add(EntityOxmReferenceHelper.getInstance());
    OxmModelLoader.registerExternalOxmModelProcessors(externalOxmModelProcessors);
    OxmModelLoader.loadModels();
    oxmVersionContextMap = OxmModelLoader.getVersionContextMap();
    parseLatestOxmVersion();
  }

  private void parseLatestOxmVersion() {
    int latestVersion = -1;
    if (oxmVersionContextMap != null) {
      Iterator it = oxmVersionContextMap.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry pair = (Map.Entry) it.next();

        String version = pair.getKey().toString();
        int versionNum = Integer.parseInt(version.substring(1, version.length()));

        if (versionNum > latestVersion) {
          latestVersion = versionNum;
          oxmVersion = pair.getKey().toString();
        }

        logger.info(EntityEventPolicyMsgs.PROCESS_OXM_MODEL_FOUND, pair.getKey().toString());
      }
    } else {
      logger.error(EntityEventPolicyMsgs.PROCESS_OXM_MODEL_MISSING, "");
    }
  }

  public void startup() {

    // Create the indexes in the search service if they do not already exist.
    searchAgent.createSearchIndex(entitySearchIndex, entitySearchSchema);
    logger.info(EntityEventPolicyMsgs.ENTITY_EVENT_POLICY_REGISTERED);
  }


  /**
   * Convert object to json.
   *
   * @param object the object
   * @param pretty the pretty
   * @return the string
   * @throws JsonProcessingException the json processing exception
   */
  public static String convertObjectToJson(Object object, boolean pretty)
      throws JsonProcessingException {
    ObjectWriter ow;

    if (pretty) {
      ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    } else {
      ow = new ObjectMapper().writer();
    }

    return ow.writeValueAsString(object);
  }

  public void returnWithError(Exchange exchange, String payload, String errorMsg) {
    logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE, errorMsg);
    logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE, errorMsg, payload);
    setResponse(exchange, ResponseType.FAILURE, additionalInfo);
  }

  public boolean isJSONValid(String test) {
    try {
      new JSONObject(test);
    } catch (JSONException ex) {
      return false;
    }
    return true;
  }

  @Override
  public void process(Exchange exchange) throws Exception {

    long startTime = System.currentTimeMillis();
    String uebPayload = exchange.getIn().getBody().toString();
    if (uebPayload == null || !isJSONValid(uebPayload)) {
      uebPayload = exchange.getIn().getBody(String.class);
      if (uebPayload == null || !isJSONValid(uebPayload)) {
        returnWithError(exchange, uebPayload, "Invalid Payload");
        return;
      }
    }

    
    JSONObject mainJson = new JSONObject(uebPayload);
    String action = mainJson.getString(OPERATION_KEY);
    if (action == null || !SUPPORTED_ACTIONS.contains(action.toLowerCase())) {
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Unrecognized action '" + action + "'", uebPayload);
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Unrecognized action '" + action + "'");
      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }

    // Load the UEB payload data, any errors will result in a failure and discard

    JSONObject spikeObjVertex = getUebContentAsJson(uebPayload, EVENT_VERTEX);
    if (spikeObjVertex == null) {
      returnWithError(exchange, uebPayload, "Payload is missing " + EVENT_VERTEX);
      return;
    }

    SpikeEventVertex eventVertex = initializeSpikeEventVertex(spikeObjVertex.toString());

    DynamicJAXBContext oxmJaxbContext = loadOxmContext(oxmVersion.toLowerCase());
    if (oxmJaxbContext == null) {
      logger.error(EntityEventPolicyMsgs.OXM_VERSION_NOT_SUPPORTED, oxmVersion);
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE, "OXM version mismatch", uebPayload);

      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }



    String entityType = eventVertex.getType();
    if (entityType == null || entityType.isEmpty()) {
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Payload header missing entity type", uebPayload);
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Payload header missing entity type");

      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }

    String entityKey = eventVertex.getKey();
    if (entityKey == null || entityKey.isEmpty()) {
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE, "Payload vertex missing entity key",
          uebPayload);
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Payload vertex missing entity key");

      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }
    String entityLink = eventVertex.getEntityLink();
    if (entityLink == null || entityLink.isEmpty()) {
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Payload header missing entity link", uebPayload);
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Payload header missing entity link");

      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }

    // log the fact that all data are in good shape
    logger.info(EntityEventPolicyMsgs.PROCESS_ENTITY_EVENT_POLICY_NONVERBOSE, action, entityType);
    logger.debug(EntityEventPolicyMsgs.PROCESS_ENTITY_EVENT_POLICY_VERBOSE, action, entityType,
        uebPayload);


    // Process for building SpikeEventEntity object
    String[] entityTypeArr = entityType.split("-");
    String oxmEntityType = "";
    for (String entityWord : entityTypeArr) {
      oxmEntityType += entityWord.substring(0, 1).toUpperCase() + entityWord.substring(1);
    }

    List<String> searchableAttr =
        getOxmAttributes(oxmJaxbContext, oxmEntityType, entityType, "searchable");
    if (searchableAttr == null) {
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Searchable attribute not found for payload entity type '" + entityType + "'");
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Searchable attribute not found for payload entity type '" + entityType + "'",
          uebPayload);

      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }

    String entityPrimaryKeyFieldName =
        getEntityPrimaryKeyFieldName(oxmJaxbContext, uebPayload, oxmEntityType, entityType);
    if (entityPrimaryKeyFieldName == null) {
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Payload missing primary key attribute");
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Payload missing primary key attribute", uebPayload);
      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }
    String entityPrimaryKeyFieldValue = lookupValueUsingKey(uebPayload, entityPrimaryKeyFieldName);
    if (entityPrimaryKeyFieldValue.isEmpty()) {
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Payload missing primary value attribute");
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Payload missing primary value attribute", uebPayload);

      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }

    SpikeEventEntity spikeEventEntity = new SpikeEventEntity();

    /*
     * Use the OXM Model to determine the primary key field name based on the entity-type
     */

    spikeEventEntity.setEntityPrimaryKeyName(entityPrimaryKeyFieldName);
    spikeEventEntity.setEntityPrimaryKeyValue(entityPrimaryKeyFieldValue);
    spikeEventEntity.setEntityType(entityType);
    spikeEventEntity.setLink(entityLink);

    if (!getSearchTags(spikeEventEntity, searchableAttr, uebPayload, action)) {
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Payload missing searchable attribute for entity type '" + entityType + "'");
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Payload missing searchable attribute for entity type '" + entityType + "'", uebPayload);

      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }

    try {
      spikeEventEntity.deriveFields();

    } catch (NoSuchAlgorithmException e) {
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE, "Cannot create unique SHA digest");
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE, "Cannot create unique SHA digest",
          uebPayload);

      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }


    handleSearchServiceOperation(spikeEventEntity, action, entitySearchIndex);

    long stopTime = System.currentTimeMillis();
    metricsLogger.info(EntityEventPolicyMsgs.OPERATION_RESULT_NO_ERRORS, PROCESS_SPIKE_EVENT,
        String.valueOf(stopTime - startTime));

    setResponse(exchange, ResponseType.SUCCESS, additionalInfo);
    return;
  }



  private void setResponse(Exchange exchange, ResponseType responseType, String additionalInfo) {

    exchange.getOut().setHeader("ResponseType", responseType.toString());
    exchange.getOut().setBody(additionalInfo);
  }


  /*
   * Load the UEB JSON payload, any errors would result to a failure case response.
   */
  private JSONObject getUebContentAsJson(String payload, String contentKey) {

    JSONObject uebJsonObj;
    JSONObject uebObjContent;

    try {
      uebJsonObj = new JSONObject(payload);
    } catch (JSONException e) {
      logger.debug(EntityEventPolicyMsgs.UEB_INVALID_PAYLOAD_JSON_FORMAT, payload);
      logger.error(EntityEventPolicyMsgs.UEB_INVALID_PAYLOAD_JSON_FORMAT, payload);
      return null;
    }

    if (uebJsonObj.has(contentKey)) {
      uebObjContent = uebJsonObj.getJSONObject(contentKey);
    } else {
      logger.debug(EntityEventPolicyMsgs.UEB_FAILED_TO_PARSE_PAYLOAD, contentKey);
      logger.error(EntityEventPolicyMsgs.UEB_FAILED_TO_PARSE_PAYLOAD, contentKey);
      return null;
    }

    return uebObjContent;
  }


  private SpikeEventVertex initializeSpikeEventVertex(String payload) {

    SpikeEventVertex eventVertex = null;
    ObjectMapper mapper = new ObjectMapper();

    // Make sure that were were actually passed in a valid string.
    if (payload == null || payload.isEmpty()) {
      logger.debug(EntityEventPolicyMsgs.UEB_FAILED_TO_PARSE_PAYLOAD, EVENT_VERTEX);
      logger.error(EntityEventPolicyMsgs.UEB_FAILED_TO_PARSE_PAYLOAD, EVENT_VERTEX);

      return eventVertex;
    }

    // Marshal the supplied string into a UebEventHeader object.
    try {
      eventVertex = mapper.readValue(payload, SpikeEventVertex.class);
    } catch (JsonProcessingException e) {
      logger.error(EntityEventPolicyMsgs.UEB_FAILED_UEBEVENTHEADER_CONVERSION, e.toString());
    } catch (Exception e) {
      logger.error(EntityEventPolicyMsgs.UEB_FAILED_UEBEVENTHEADER_CONVERSION, e.toString());
    }

    if (eventVertex != null) {
      logger.debug(EntityEventPolicyMsgs.UEB_EVENT_HEADER_PARSED, eventVertex.toString());
    }

    return eventVertex;

  }


  private String getEntityPrimaryKeyFieldName(DynamicJAXBContext oxmJaxbContext, String payload,
      String oxmEntityType, String entityType) {

    DynamicType entity = oxmJaxbContext.getDynamicType(oxmEntityType);
    if (entity == null) {
      return null;
    }

    List<DatabaseField> list = entity.getDescriptor().getPrimaryKeyFields();
    if (list != null && !list.isEmpty()) {
      String keyName = list.get(0).getName();
      return keyName.substring(0, keyName.indexOf('/'));
    }

    return "";
  }

  private String lookupValueUsingKey(String payload, String key) throws JSONException {
    JsonNode jsonNode = convertToJsonNode(payload);
    return RouterServiceUtil.recursivelyLookupJsonPayload(jsonNode, key);
  }


  private JsonNode convertToJsonNode(String payload) {

    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonNode = null;
    try {
      jsonNode = mapper.readTree(mapper.getJsonFactory().createJsonParser(payload));
    } catch (IOException e) {
      logger.debug(EntityEventPolicyMsgs.FAILED_TO_PARSE_UEB_PAYLOAD, EVENT_VERTEX + " missing",
          payload);
      logger.error(EntityEventPolicyMsgs.FAILED_TO_PARSE_UEB_PAYLOAD, EVENT_VERTEX + " missing",
          "");
    }

    return jsonNode;
  }


  private boolean getSearchTags(SpikeEventEntity spikeEventEntity, List<String> searchableAttr,
      String payload, String action) {

    boolean hasSearchableAttr = false;
    for (String searchTagField : searchableAttr) {
      String searchTagValue;
      if (searchTagField.equalsIgnoreCase(spikeEventEntity.getEntityPrimaryKeyName())) {
        searchTagValue = spikeEventEntity.getEntityPrimaryKeyValue();
      } else {
        searchTagValue = this.lookupValueUsingKey(payload, searchTagField);
      }

      if (searchTagValue != null && !searchTagValue.isEmpty()) {
        hasSearchableAttr = true;
        spikeEventEntity.addSearchTagWithKey(searchTagValue, searchTagField);
      }
    }
    return hasSearchableAttr;
  }

  /*
   * Check if OXM version is available. If available, load it.
   */
  private DynamicJAXBContext loadOxmContext(String version) {
    if (version == null) {
      logger.error(EntityEventPolicyMsgs.FAILED_TO_FIND_OXM_VERSION, version);
      return null;
    }

    return oxmVersionContextMap.get(version);
  }

  private List<String> getOxmAttributes(DynamicJAXBContext oxmJaxbContext, String oxmEntityType,
      String entityType, String fieldName) {

    DynamicType entity = (DynamicType) oxmJaxbContext.getDynamicType(oxmEntityType);
    if (entity == null) {
      return null;
    }

    /*
     * Check for searchable XML tag
     */
    List<String> fieldValues = null;
    Map<String, String> properties = entity.getDescriptor().getProperties();
    for (Map.Entry<String, String> entry : properties.entrySet()) {
      if (entry.getKey().equalsIgnoreCase(fieldName)) {
        fieldValues = Arrays.asList(entry.getValue().split(","));
        break;
      }
    }

    return fieldValues;
  }



  protected SpikeEventEntity getPopulatedEntity(JsonNode entityNode,
      OxmEntityDescriptor resultDescriptor) {
    SpikeEventEntity d = new SpikeEventEntity();

    d.setEntityType(resultDescriptor.getEntityName());

    List<String> primaryKeyValues = new ArrayList<>();
    List<String> primaryKeyNames = new ArrayList<>();
    String pkeyValue;

    for (String keyName : resultDescriptor.getPrimaryKeyAttributeName()) {
      pkeyValue = RouterServiceUtil.getNodeFieldAsText(entityNode, keyName);
      if (pkeyValue != null) {
        primaryKeyValues.add(pkeyValue);
        primaryKeyNames.add(keyName);
      } else {
        // logger.warn("getPopulatedDocument(), pKeyValue is null for entityType = " +
        // resultDescriptor.getEntityName());
        logger.error(EntityEventPolicyMsgs.PRIMARY_KEY_NULL_FOR_ENTITY_TYPE,
            resultDescriptor.getEntityName());
      }
    }

    final String primaryCompositeKeyValue = RouterServiceUtil.concatArray(primaryKeyValues, "/");
    d.setEntityPrimaryKeyValue(primaryCompositeKeyValue);
    final String primaryCompositeKeyName = RouterServiceUtil.concatArray(primaryKeyNames, "/");
    d.setEntityPrimaryKeyName(primaryCompositeKeyName);

    final List<String> searchTagFields = resultDescriptor.getSearchableAttributes();

    /*
     * Based on configuration, use the configured field names for this entity-Type to build a
     * multi-value collection of search tags for elastic search entity search criteria.
     */


    for (String searchTagField : searchTagFields) {
      String searchTagValue = RouterServiceUtil.getNodeFieldAsText(entityNode, searchTagField);
      if (searchTagValue != null && !searchTagValue.isEmpty()) {
        d.addSearchTagWithKey(searchTagValue, searchTagField);
      }
    }

    return d;
  }


  /**
   * Perform create, read, update or delete (CRUD) operation on search engine's suggestive search
   * index
   * 
   * @param eventEntity Entity/data to use in operation
   * @param action The operation to perform
   * @param target Resource to perform the operation on
   * @param allowDeleteEvent Allow delete operation to be performed on resource
   */
  protected void handleSearchServiceOperation(DocumentStoreDataEntity eventEntity, String action,
      String index) {
    try {

      Map<String, List<String>> headers = new HashMap<>();
      headers.put(Headers.FROM_APP_ID, Arrays.asList("DataLayer"));
      headers.put(Headers.TRANSACTION_ID, Arrays.asList(MDC.get(MdcContext.MDC_REQUEST_ID)));

      String entityId = eventEntity.getId();

      if ((action.equalsIgnoreCase(ACTION_CREATE) && entityId != null)
          || action.equalsIgnoreCase(ACTION_UPDATE)) {

        // Run the GET to retrieve the ETAG from the search service
        OperationResult storedEntity = searchAgent.getDocument(index, entityId);

        if (HttpUtil.isHttpResponseClassSuccess(storedEntity.getResultCode())) {
          List<String> etag = storedEntity.getHeaders().get(Headers.ETAG);

          if (etag != null && !etag.isEmpty()) {
            headers.put(Headers.IF_MATCH, etag);
          } else {
            logger.error(EntityEventPolicyMsgs.NO_ETAG_AVAILABLE_FAILURE, index, entityId);
          }
        }

        // Write the entity to the search service.
        // PUT
        searchAgent.putDocument(index, entityId, eventEntity.getAsJson(), headers);
      } else if (action.equalsIgnoreCase(ACTION_CREATE)) {
        // Write the entry to the search service.
        searchAgent.postDocument(index, eventEntity.getAsJson(), headers);

      } else if (action.equalsIgnoreCase(ACTION_DELETE)) {
        // Run the GET to retrieve the ETAG from the search service
        OperationResult storedEntity = searchAgent.getDocument(index, entityId);

        if (HttpUtil.isHttpResponseClassSuccess(storedEntity.getResultCode())) {
          List<String> etag = storedEntity.getHeaders().get(Headers.ETAG);

          if (etag != null && !etag.isEmpty()) {
            headers.put(Headers.IF_MATCH, etag);
          } else {
            logger.error(EntityEventPolicyMsgs.NO_ETAG_AVAILABLE_FAILURE, index, entityId);
          }

          searchAgent.deleteDocument(index, eventEntity.getId(), headers);
        } else {
          logger.error(EntityEventPolicyMsgs.NO_ETAG_AVAILABLE_FAILURE, index, entityId);
        }
      } else {
        logger.error(EntityEventPolicyMsgs.ENTITY_OPERATION_NOT_SUPPORTED, action);
      }
    } catch (IOException e) {
      logger.error(EntityEventPolicyMsgs.FAILED_TO_UPDATE_ENTITY_IN_DOCSTORE, eventEntity.getId(),
          action);
    }
  }



  // put this here until we find a better spot
  /**
   * Helper utility to concatenate substrings of a URI together to form a proper URI.
   * 
   * @param suburis the list of substrings to concatenate together
   * @return the concatenated list of substrings
   */
  public static String concatSubUri(String... suburis) {
    String finalUri = "";

    for (String suburi : suburis) {

      if (suburi != null) {
        // Remove any leading / since we only want to append /
        suburi = suburi.replaceFirst("^/*", "");

        // Add a trailing / if one isn't already there
        finalUri += suburi.endsWith("/") ? suburi : suburi + "/";
      }
    }

    return finalUri;
  }
}

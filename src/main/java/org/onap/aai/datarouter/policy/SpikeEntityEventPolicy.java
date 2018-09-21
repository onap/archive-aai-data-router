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
import java.util.Map.Entry;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.eclipse.persistence.oxm.MediaType;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.cl.mdc.MdcContext;
import org.onap.aai.datarouter.entity.DocumentStoreDataEntity;
import org.onap.aai.datarouter.entity.SpikeEventEntity;
import org.onap.aai.datarouter.entity.SpikeEventVertex;
import org.onap.aai.datarouter.logging.EntityEventPolicyMsgs;
import org.onap.aai.datarouter.util.RouterServiceUtil;
import org.onap.aai.datarouter.util.SearchServiceAgent;
import org.onap.aai.entity.OxmEntityDescriptor;
import org.onap.aai.util.EntityOxmReferenceHelper;
import org.onap.aai.util.ExternalOxmModelProcessor;
import org.onap.aai.schema.OxmModelLoader;
import org.onap.aai.setup.SchemaVersions;
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
  private static final String ENTITY_SEARCH_SCHEMA = "entitysearch_schema.json";

  private Collection<ExternalOxmModelProcessor> externalOxmModelProcessors;

  /**
   * Note (8-June-2018):
   * 
   * At present we don't need to support every event-type that could be present in the spike-events.
   * The only one we want is a SPIKE "update-notification". In the future perhaps we need to add some
   * configurability to the camel-route itself with a json camel filtering component so that routing
   * logic can be modified as part of the camel route spring-xml instead of hard-coding the
   * event filtering in here.
   */
  
  private static final String PROCESS_SPIKE_EVENT = "Process Spike Event";
  
  private static final String UPDATE_NOTIFICATION = "update-notification";
  private static final String SPIKE = "SPIKE";
  
  private static final String HEADER_KEY = "header";
  private static final String EVENT_TYPE_KEY = "event-type";
  private static final String SOURCE_NAME_KEY = "source-name";
  private static final String BODY_KEY = "body";
  private static final String OPERATION_KEY = "operation";

  private static final String VERTEX_KEY = "vertex";
  private static final String VERTEX_KEY_KEY = "key";
  private static final String VERTEX_TYPE_KEY = "type";
  private static final String VERTEX_SCHEMA_VERSION_KEY = "schema-version";
  
  private static final String CREATE = "create";
  private static final String DELETE = "delete";
  private static final String UPDATE = "update";

  private static final List<String> SUPPORTED_ACTIONS =
      Arrays.asList(CREATE, UPDATE, DELETE);

  Map<String, DynamicJAXBContext> oxmVersionContextMap = new HashMap<>();
  private String oxmVersion = null;

  /** Agent for communicating with the Search Service. */
  private SearchServiceAgent searchAgent = null;
  private String entitySearchIndex;

  private Logger logger;
  private Logger metricsLogger;
  
  public enum ResponseType {
    SUCCESS, PARTIAL_SUCCESS, FAILURE;
  };

  public SpikeEntityEventPolicy(SpikeEntityEventPolicyConfig config) throws FileNotFoundException {
    LoggerFactory loggerFactoryInstance = LoggerFactory.getInstance();
    logger = loggerFactoryInstance.getLogger(SpikeEntityEventPolicy.class.getName());
    metricsLogger = loggerFactoryInstance.getMetricsLogger(SpikeEntityEventPolicy.class.getName());


    //srcDomain = config.getSourceDomain();

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
    OxmModelLoader.loadModels(config.getSchemaVersions(), config.getSchemaLocationsBean());
    oxmVersionContextMap = OxmModelLoader.getVersionContextMap();
    parseLatestOxmVersion();
  }

  private void parseLatestOxmVersion() {
    int latestVersion = -1;
    if (oxmVersionContextMap != null) {
      Iterator<Entry<String, DynamicJAXBContext>> it = oxmVersionContextMap.entrySet().iterator();
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
    searchAgent.createSearchIndex(entitySearchIndex, ENTITY_SEARCH_SCHEMA);
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

  @Override
  public void process(Exchange exchange) /*throws Exception*/ {

    long startTime = System.currentTimeMillis();
    
    final String eventPayload = exchange.getIn().getBody().toString();
    JSONObject mainJson = null;
    
    try {
      
      /*
       * It is expected that mainJson will have multiple top level objects:
       *  - header
       *  - body
       *  - result
       */
      
      mainJson = new JSONObject(eventPayload);
    } catch (JSONException exc) {
      returnWithError(exchange, eventPayload, "Invalid Payload");
      return;
    }
    
    JSONObject eventHeader = mainJson.getJSONObject(HEADER_KEY);

    if (eventHeader == null) {
      returnWithError(exchange, eventPayload, "Payload is missing " + HEADER_KEY);
      return;
    }
    
    /*
     * Only process SPIKE update-notification events 
     */
    
    final String sourceName = eventHeader.getString(SOURCE_NAME_KEY);
    final String eventType = eventHeader.getString(EVENT_TYPE_KEY);

    if (!(SPIKE.equals(sourceName) && UPDATE_NOTIFICATION.equals(eventType))) {
      // drop event
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE, "Ignoring event with sourceName='"
          + sourceName + "' and eventType='" + eventType + "'.  Payload=" + eventPayload);
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Ignoring event with sourceName='" + sourceName + "' and eventType='" + eventType + "'.");

      /*
       * I don't think ignoring a non-applicable event constitutes a failure.
       */

      setResponse(exchange, ResponseType.SUCCESS, additionalInfo);
      return;
    }
    
    JSONObject eventBody = mainJson.getJSONObject(BODY_KEY);

    if (eventBody == null) {
      returnWithError(exchange, eventPayload, "Payload is missing " + BODY_KEY);
      return;
    }
    
    String action = eventBody.getString(OPERATION_KEY);
    if (action == null || !SUPPORTED_ACTIONS.contains(action.toLowerCase())) {
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Unrecognized action '" + action + "'", eventPayload);
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Unrecognized action '" + action + "'");
      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }

    // Load the event body data, any errors will result in a failure and discard
    
    JSONObject spikeVertex = eventBody.getJSONObject(VERTEX_KEY);
    if (spikeVertex == null) {
      returnWithError(exchange, eventPayload, "Payload is missing " + VERTEX_KEY);
      return;
    }

    SpikeEventVertex eventVertex = null;
    try {
      eventVertex = initializeSpikeEventVertex(spikeVertex);
    } catch (JSONException exc) {
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE, "Error initializating spike event.  Error: " + exc.getMessage(),
          eventPayload);
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Error initializating spike event.  Error: " + exc.getMessage());

      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }

    DynamicJAXBContext oxmJaxbContext = loadOxmContext(oxmVersion);
    if (oxmJaxbContext == null) {
      logger.error(EntityEventPolicyMsgs.OXM_VERSION_NOT_SUPPORTED, oxmVersion);
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE, "OXM version mismatch", eventPayload);

      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }

    String entityType = eventVertex.getType();
    if (entityType == null || entityType.isEmpty()) {
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Payload header missing entity type", eventPayload);
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Payload header missing entity type");

      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }

    String entityKey = eventVertex.getKey();
    if (entityKey == null || entityKey.isEmpty()) {
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE, "Payload vertex missing entity key",
          eventPayload);
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Payload vertex missing entity key");

      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }
    String entityLink = eventVertex.getEntityLink();
    if (entityLink == null || entityLink.isEmpty()) {
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Payload header missing entity link", eventPayload);
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Payload header missing entity link");

      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }

    // log the fact that all data are in good shape
    logger.info(EntityEventPolicyMsgs.PROCESS_ENTITY_EVENT_POLICY_NONVERBOSE, action, entityType);
    logger.debug(EntityEventPolicyMsgs.PROCESS_ENTITY_EVENT_POLICY_VERBOSE, action, entityType,
        eventPayload);

    // Process for building SpikeEventEntity object
    String oxmEntityType = new OxmEntityTypeConverter().convert(entityType);
    
    List<String> searchableAttr =
        getOxmAttributes(oxmJaxbContext, oxmEntityType, entityType, "searchable");
    if (searchableAttr == null) {
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Searchable attribute not found for payload entity type '" + entityType + "'");
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Searchable attribute not found for payload entity type '" + entityType + "'",
          eventPayload);

      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }

    String entityPrimaryKeyFieldName =
        getEntityPrimaryKeyFieldName(oxmJaxbContext, eventPayload, oxmEntityType, entityType);
    if (entityPrimaryKeyFieldName == null) {
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Payload missing primary key attribute");
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Payload missing primary key attribute", eventPayload);
      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }
    String entityPrimaryKeyFieldValue = lookupValueUsingKey(eventPayload, entityPrimaryKeyFieldName);
    if (entityPrimaryKeyFieldValue == null || entityPrimaryKeyFieldValue.isEmpty()) {
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Payload missing primary value attribute");
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Payload missing primary value attribute", eventPayload);

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
    
    System.out.println(spikeEventEntity);

    if (!getSearchTags(spikeEventEntity, searchableAttr, eventPayload, action)) {
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Payload missing searchable attribute for entity type '" + entityType + "'");
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Payload missing searchable attribute for entity type '" + entityType + "'", eventPayload);

      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }

    try {
      spikeEventEntity.deriveFields();

    } catch (NoSuchAlgorithmException e) {
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE, "Cannot create unique SHA digest");
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE, "Cannot create unique SHA digest",
          eventPayload);

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

  private SpikeEventVertex initializeSpikeEventVertex(JSONObject vertexObject) throws JSONException {

    /*
     * These are all critical keys
     */

    final String vertexType = vertexObject.getString(VERTEX_TYPE_KEY);
    final String vertexKey = vertexObject.getString(VERTEX_KEY_KEY);
    final String vertexSchemaVersion = vertexObject.getString(VERTEX_SCHEMA_VERSION_KEY);

    SpikeEventVertex eventVertex = new SpikeEventVertex(vertexType, vertexKey);
    eventVertex.setSchemaVersion(vertexSchemaVersion);
    logger.debug(EntityEventPolicyMsgs.UEB_EVENT_HEADER_PARSED, eventVertex.toString());

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

  private String lookupValueUsingKey(String payload, String key) {
    JsonNode jsonNode = convertToJsonNode(payload);
    return RouterServiceUtil.recursivelyLookupJsonPayload(jsonNode, key);
  }


  private JsonNode convertToJsonNode(String payload) {

    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonNode = null;
    try {
      jsonNode = mapper.readTree(mapper.getJsonFactory().createJsonParser(payload));
    } catch (IOException e) {
      logger.debug(EntityEventPolicyMsgs.FAILED_TO_PARSE_UEB_PAYLOAD, VERTEX_KEY + " missing",
          payload);
      logger.error(EntityEventPolicyMsgs.FAILED_TO_PARSE_UEB_PAYLOAD, VERTEX_KEY + " missing",
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

    DynamicType entity = oxmJaxbContext.getDynamicType(oxmEntityType);
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

      if ((action.equalsIgnoreCase(CREATE) && entityId != null)
          || action.equalsIgnoreCase(UPDATE)) {

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
      } else if (action.equalsIgnoreCase(CREATE)) {
        // Write the entry to the search service.
        searchAgent.postDocument(index, eventEntity.getAsJson(), headers);

      } else if (action.equalsIgnoreCase(DELETE)) {
        // Run the GET to retrieve the ETAG from the search service
        OperationResult storedEntity = searchAgent.getDocument(index, entityId);

        if (HttpUtil.isHttpResponseClassSuccess(storedEntity.getResultCode())) {
          List<String> etag = storedEntity.getHeaders().get(Headers.ETAG);

          if (etag != null && !etag.isEmpty()) {
            headers.put(Headers.IF_MATCH, etag);
          } else {
            logger.error(EntityEventPolicyMsgs.NO_ETAG_AVAILABLE_FAILURE, index, entityId);
          }
          
          /*
           * The Spring-Boot version of the search-data-service rejects the DELETE operation unless
           * we specify a Content-Type.
           */

          headers.put("Content-Type", Arrays.asList(MediaType.APPLICATION_JSON.getMediaType()));

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

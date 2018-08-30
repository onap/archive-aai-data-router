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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.eclipse.persistence.oxm.MediaType;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.aai.datarouter.entity.AaiEventEntity;
import org.onap.aai.datarouter.entity.AggregationEntity;
import org.onap.aai.datarouter.entity.DocumentStoreDataEntity;
import org.onap.aai.datarouter.entity.SuggestionSearchEntity;
import org.onap.aai.datarouter.entity.TopographicalEntity;
import org.onap.aai.datarouter.entity.UebEventHeader;
import org.onap.aai.datarouter.logging.EntityEventPolicyMsgs;
import org.onap.aai.datarouter.util.NodeUtils;
import org.onap.aai.datarouter.util.RouterServiceUtil;
import org.onap.aai.datarouter.util.SearchServiceAgent;
import org.onap.aai.datarouter.util.SearchSuggestionPermutation;
import org.onap.aai.entity.OxmEntityDescriptor;
import org.onap.aai.util.CrossEntityReference;
import org.onap.aai.util.EntityOxmReferenceHelper;
import org.onap.aai.util.ExternalOxmModelProcessor;
import org.onap.aai.schema.OxmModelLoader;
import org.onap.aai.util.Version;
import org.onap.aai.util.VersionedOxmEntities;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.cl.mdc.MdcContext;
import org.onap.aai.restclient.client.Headers;
import org.onap.aai.restclient.client.OperationResult;
import org.onap.aai.restclient.rest.HttpUtil;
import org.slf4j.MDC;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class EntityEventPolicy implements Processor {

  public static final String additionalInfo = "Response of AAIEntityEventPolicy";
  private static final String ENTITY_SEARCH_SCHEMA = "entitysearch_schema.json";
  private static final String TOPOGRAPHICAL_SEARCH_SCHEMA = "topographysearch_schema.json";
  private Collection<ExternalOxmModelProcessor> externalOxmModelProcessors;

  private static final String EVENT_HEADER = "event-header";
  private static final String ENTITY_HEADER = "entity";
  private static final String ACTION_CREATE = "create";
  private static final String ACTION_DELETE = "delete";
  private static final String ACTION_UPDATE = "update";
  private static final String PROCESS_AAI_EVENT = "Process AAI Event";
  private static final String TOPO_LAT = "latitude";
  private static final String TOPO_LONG = "longitude";

  private static final List<String> SUPPORTED_ACTIONS =
      Arrays.asList(ACTION_CREATE, ACTION_UPDATE, ACTION_DELETE);

  Map<String, DynamicJAXBContext> oxmVersionContextMap = new HashMap<>();
  private String oxmVersion = null;

  /** Agent for communicating with the Search Service. */
  private SearchServiceAgent searchAgent = null;

  /** Search index name for storing AAI event entities. */
  private String entitySearchIndex;

  /** Search index name for storing topographical search data. */
  private String topographicalSearchIndex;

  /** Search index name for suggestive search data. */
  private String aggregateGenericVnfIndex;

  private String autosuggestIndex;

  private String srcDomain;

  private Logger logger;
  private Logger metricsLogger;

  public enum ResponseType {
    SUCCESS, PARTIAL_SUCCESS, FAILURE;
  };

  public EntityEventPolicy(EntityEventPolicyConfig config) throws FileNotFoundException {
    LoggerFactory loggerFactoryInstance = LoggerFactory.getInstance();
    logger = loggerFactoryInstance.getLogger(EntityEventPolicy.class.getName());
    metricsLogger = loggerFactoryInstance.getMetricsLogger(EntityEventPolicy.class.getName());


    srcDomain = config.getSourceDomain();

    // Populate the index names.
    entitySearchIndex        = config.getSearchEntitySearchIndex();
    topographicalSearchIndex = config.getSearchTopographySearchIndex();
    aggregateGenericVnfIndex = config.getSearchAggregationVnfIndex();
    autosuggestIndex		 = config.getSearchEntityAutoSuggestIndex();

    // Instantiate the agent that we will use for interacting with the Search Service.
    searchAgent = new SearchServiceAgent(config.getSearchCertName(),
                                         config.getSearchKeystore(),
                                         config.getSearchKeystorePwd(),
                                         EntityEventPolicy.concatSubUri(config.getSearchBaseUrl(),
                                                                        config.getSearchEndpoint()),
                                         config.getSearchEndpointDocuments(),
                                         logger);

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
    searchAgent.createSearchIndex(entitySearchIndex, ENTITY_SEARCH_SCHEMA);
    searchAgent.createSearchIndex(topographicalSearchIndex, TOPOGRAPHICAL_SEARCH_SCHEMA);

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

  public void returnWithError(Exchange exchange, String payload, String errorMsg){
    logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE, errorMsg);
    logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE, errorMsg, payload);
    setResponse(exchange, ResponseType.FAILURE, additionalInfo);
  }

  @Override
  public void process(Exchange exchange) throws Exception {

    long startTime = System.currentTimeMillis();

    String uebPayload = exchange.getIn().getBody().toString();

    JsonNode uebAsJson =null;
    ObjectMapper mapper = new ObjectMapper();
    try{
      uebAsJson = mapper.readTree(uebPayload);
    } catch (IOException e){
      returnWithError(exchange, uebPayload, "Invalid Payload");
      return;
    }

    // Load the UEB payload data, any errors will result in a failure and discard
    JSONObject uebObjHeader = getUebContentAsJson(uebPayload, EVENT_HEADER);
    if (uebObjHeader == null) {
      returnWithError(exchange, uebPayload, "Payload is missing " + EVENT_HEADER);
      return;
    }

    JSONObject uebObjEntity = getUebContentAsJson(uebPayload, ENTITY_HEADER);
    if (uebObjEntity == null) {
      returnWithError(exchange, uebPayload, "Payload is missing " + ENTITY_HEADER);
      return;
    }

    UebEventHeader eventHeader;
    eventHeader = initializeUebEventHeader(uebObjHeader.toString());

    // Get src domain from header; discard event if not originated from same domain
    String payloadSrcDomain = eventHeader.getDomain();
    if (payloadSrcDomain == null || !payloadSrcDomain.equalsIgnoreCase(this.srcDomain)) {
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Unrecognized source domain '" + payloadSrcDomain + "'", uebPayload);
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Unrecognized source domain '" + payloadSrcDomain + "'");

      setResponse(exchange, ResponseType.SUCCESS, additionalInfo);
      return;
    }

    DynamicJAXBContext oxmJaxbContext = loadOxmContext(oxmVersion);
    if (oxmJaxbContext == null) {
      logger.error(EntityEventPolicyMsgs.OXM_VERSION_NOT_SUPPORTED, oxmVersion);
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE, "OXM version mismatch",
          uebPayload);

      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }

    String action = eventHeader.getAction();
    if (action == null || !SUPPORTED_ACTIONS.contains(action.toLowerCase())) {
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Unrecognized action '" + action + "'", uebPayload);
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Unrecognized action '" + action + "'");

      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }

    String entityType = eventHeader.getEntityType();
    if (entityType == null) {
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Payload header missing entity type", uebPayload);
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Payload header missing entity type");

      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }

    String topEntityType = eventHeader.getTopEntityType();
    if (topEntityType == null) {
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Payload header missing top entity type", uebPayload);
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Payload header top missing entity type");

      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }

    String entityLink = eventHeader.getEntityLink();
    if (entityLink == null) {
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Payload header missing entity link", uebPayload);
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Payload header missing entity link");

      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }

    // log the fact that all data are in good shape
    logger.info(EntityEventPolicyMsgs.PROCESS_ENTITY_EVENT_POLICY_NONVERBOSE, action,
        entityType);
    logger.debug(EntityEventPolicyMsgs.PROCESS_ENTITY_EVENT_POLICY_VERBOSE, action, entityType,
        uebPayload);


    // Process for building AaiEventEntity object
    String oxmEntityType = new OxmEntityTypeConverter().convert(entityType);

    List<String> searchableAttr =
        getOxmAttributes(uebPayload, oxmJaxbContext, oxmEntityType, entityType, "searchable");
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
    String entityPrimaryKeyFieldValue = lookupValueUsingKey(uebPayload, entityPrimaryKeyFieldName);
    if (entityPrimaryKeyFieldValue == null || entityPrimaryKeyFieldValue.isEmpty()) {
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Payload missing primary key attribute");
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Payload missing primary key attribute", uebPayload);

      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }

    AaiEventEntity aaiEventEntity = new AaiEventEntity();

    /*
     * Use the OXM Model to determine the primary key field name based on the entity-type
     */

    aaiEventEntity.setEntityPrimaryKeyName(entityPrimaryKeyFieldName);
    aaiEventEntity.setEntityPrimaryKeyValue(entityPrimaryKeyFieldValue);
    aaiEventEntity.setEntityType(entityType);
    aaiEventEntity.setLink(entityLink);

    if (!getSearchTags(aaiEventEntity, searchableAttr, uebPayload, action)) {
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Payload missing searchable attribute for entity type '" + entityType + "'");
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Payload missing searchable attribute for entity type '" + entityType + "'", uebPayload);

      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;

    }

    try {
      aaiEventEntity.deriveFields();

    } catch (NoSuchAlgorithmException e) {
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Cannot create unique SHA digest");
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Cannot create unique SHA digest", uebPayload);

      setResponse(exchange, ResponseType.FAILURE, additionalInfo);
      return;
    }

    handleSearchServiceOperation(aaiEventEntity, action, entitySearchIndex);

    handleTopographicalData(uebPayload, action, entityType, oxmEntityType, oxmJaxbContext,
        entityPrimaryKeyFieldName, entityPrimaryKeyFieldValue);

    /*
     * Use the versioned OXM Entity class to get access to cross-entity reference helper collections
     */
    VersionedOxmEntities oxmEntities =
        EntityOxmReferenceHelper.getInstance().getVersionedOxmEntities(Version.valueOf(oxmVersion.toLowerCase()));

    /**
     * NOTES:
     * 1. If the entity type is "customer", the below check will return true if any nested entityType
     * in that model could contain a CER based on the OXM model version that has been loaded.
     * 2. For a DELETE operation on outer/parent entity (handled by the regular flow:
     * handleSearchServiceOperation()), ignore processing for cross-entity-reference under the
     * assumption that AAI will push down all required cascade-deletes for nested entities as well
     * 3. Handling the case where UEB events arrive out of order: CREATE customer is received before
     *  CREATE service-instance.
     */

    if (!action.equalsIgnoreCase(ACTION_DELETE) && oxmEntities != null
        && oxmEntities.entityModelContainsCrossEntityReference(topEntityType)) {

      // We know the model "can" contain a CER reference definition, let's process a bit more

      HashMap<String, CrossEntityReference> crossEntityRefMap =
          oxmEntities.getCrossEntityReferences();

      JSONObject entityJsonObject = getUebEntity(uebPayload);

      JsonNode entityJsonNode = null;

      if (entityJsonObject != null) {
          entityJsonNode = convertToJsonNode(entityJsonObject.toString());

          String parentEntityType = entityType;

          String targetEntityUrl = entityLink;

          for (Map.Entry<String, CrossEntityReference> entry : crossEntityRefMap.entrySet()) {

              /*
               * if we know service-subscription is in the tree, then we can pull our all instances and process
               * from there.
               */

              String key = entry.getKey();
              CrossEntityReference cerDescriptor = entry.getValue();

              ArrayList<JsonNode> foundNodes = new ArrayList<>();

              RouterServiceUtil.extractObjectsByKey(entityJsonNode, key, foundNodes);

              if (!foundNodes.isEmpty()) {

                  for (JsonNode n : foundNodes) {
                      if ("customer".equalsIgnoreCase(parentEntityType)) {
                          /*
                           * NOTES: 1. prepare to hand-create url for service-instance 2. this will break if the
                           * URL structure for service-instance changes
                           */
                          if (n.has("service-type")) {
                              targetEntityUrl += "/service-subscriptions/service-subscription/"
                                      + RouterServiceUtil.getNodeFieldAsText(n, "service-type")
                                      + "/service-instances/service-instance/";
                          }
                      }

                      List<String> extractedParentEntityAttributeValues = new ArrayList<>();

                      RouterServiceUtil.extractFieldValuesFromObject(n, cerDescriptor.getAttributeNames(),
                              extractedParentEntityAttributeValues);

                      List<JsonNode> nestedTargetEntityInstances = new ArrayList<>();
                      RouterServiceUtil.extractObjectsByKey(n, cerDescriptor.getTargetEntityType(),
                                    nestedTargetEntityInstances);

                      for (JsonNode targetEntityInstance : nestedTargetEntityInstances) {
                          /*
                           * Now: 1. build the AAIEntityType (IndexDocument) based on the extract entity 2. Get
                           * data from ES 3. Extract ETAG 4. Merge ES Doc + AAIEntityType + Extracted Parent
                           * Cross-Entity-Reference Values 5. Put data into ES with ETAG + updated doc
                           */

                          // Get the complete URL for target entity
                          if (targetEntityInstance.has("link")) { // nested SI has url mentioned
                              targetEntityUrl = RouterServiceUtil.getNodeFieldAsText(targetEntityInstance, "link");
                          } else if ("customer".equalsIgnoreCase(parentEntityType) && targetEntityInstance.has("service-instance-id")) {
                                    targetEntityUrl += RouterServiceUtil.getNodeFieldAsText(targetEntityInstance, "service-instance-id");
                          }

                          OxmEntityDescriptor searchableDescriptor = oxmEntities.getSearchableEntityDescriptor(cerDescriptor.getTargetEntityType());

                          if (searchableDescriptor != null) {

                              if (!searchableDescriptor.getSearchableAttributes().isEmpty()) {

                                  AaiEventEntity entityToSync = null;

                                  try {

                                      entityToSync = getPopulatedEntity(targetEntityInstance, searchableDescriptor);

                                      /*
                                       * Ready to do some ElasticSearch ops
                                       */

                                      for (String parentCrossEntityReferenceAttributeValue : extractedParentEntityAttributeValues) {
                                          entityToSync.addCrossEntityReferenceValue(parentCrossEntityReferenceAttributeValue);
                                      }

                                      entityToSync.setLink(targetEntityUrl);
                                      entityToSync.deriveFields();

                                      updateCerInEntity(entityToSync);

                                  } catch (NoSuchAlgorithmException e) {
                                      logger.debug(e.getMessage());
                                  }
                              }
                          } else {
                              logger.debug(EntityEventPolicyMsgs.CROSS_ENTITY_REFERENCE_SYNC,
                                      "failure to find searchable descriptor for type "
                                              + cerDescriptor.getTargetEntityType());
                          }
                      }

                  }

              } else {
                  logger.debug(EntityEventPolicyMsgs.CROSS_ENTITY_REFERENCE_SYNC,
                          "failed to find 0 instances of cross-entity-reference with entity " + key);
              }

          }
      } else {
          logger.info(EntityEventPolicyMsgs.FAILED_TO_PARSE_UEB_PAYLOAD, "Unable to get UEB object");
      }

    } else {
        logger.info(EntityEventPolicyMsgs.CROSS_ENTITY_REFERENCE_SYNC, "skipped due to OXM model for "
                + topEntityType + " does not contain a cross-entity-reference entity");
    }

    /*
     * Process for autosuggestable entities
     */
    if (oxmEntities != null) {
      Map<String, OxmEntityDescriptor> rootDescriptor =
          oxmEntities.getSuggestableEntityDescriptors();
      if (!rootDescriptor.isEmpty()) {
        List<String> suggestibleAttrInPayload = new ArrayList<>();
        List<String> suggestibleAttrInOxm = extractSuggestableAttr(oxmEntities, entityType);
        if (suggestibleAttrInOxm != null) {
          for (String attr: suggestibleAttrInOxm){
            if ( uebObjEntity.has(attr) ){
              suggestibleAttrInPayload.add(attr);
            }
          }
        }

        if (suggestibleAttrInPayload.isEmpty()) {
          return;
        }

        List<String> suggestionAliases = extractAliasForSuggestableEntity(oxmEntities, entityType);
        AggregationEntity ae = new AggregationEntity();
        ae.setLink(entityLink);
        ae.deriveFields(uebAsJson);

        handleSearchServiceOperation(ae, action, aggregateGenericVnfIndex);

        /*
         * It was decided to silently ignore DELETE requests for resources we don't allow to be
         * deleted. e.g. auto-suggestion deletion is not allowed while aggregation deletion is.
         */
        if (!ACTION_DELETE.equalsIgnoreCase(action)) {
          List<ArrayList<String>> listOfValidPowerSetElements =
              SearchSuggestionPermutation.getNonEmptyUniqueLists(suggestibleAttrInPayload);

          // Now we have a list containing the power-set (minus empty element) for the status that are
          // available in the payload. Try inserting a document for every combination.
          for (ArrayList<String> list : listOfValidPowerSetElements) {
            SuggestionSearchEntity suggestionSearchEntity = new SuggestionSearchEntity();
            suggestionSearchEntity.setEntityType(entityType);
            suggestionSearchEntity.setSuggestableAttr(list);
            suggestionSearchEntity.setEntityTypeAliases(suggestionAliases);
            suggestionSearchEntity.setFilterBasedPayloadFromResponse(uebAsJson.get("entity"),
                suggestibleAttrInOxm, list);
            suggestionSearchEntity.setSuggestionInputPermutations(
                suggestionSearchEntity.generateSuggestionInputPermutations());

            if (suggestionSearchEntity.isSuggestableDoc()) {
              try {
                suggestionSearchEntity.generateSearchSuggestionDisplayStringAndId();
              } catch (NoSuchAlgorithmException e) {
                logger.error(EntityEventPolicyMsgs.DISCARD_UPDATING_SEARCH_SUGGESTION_DATA,
                    "Cannot create unique SHA digest for search suggestion data. Exception: "
                        + e.getLocalizedMessage());
              }

              handleSearchServiceOperation(suggestionSearchEntity, action, autosuggestIndex);
            }
          }
        }
      }
    }

    long stopTime = System.currentTimeMillis();

    metricsLogger.info(EntityEventPolicyMsgs.OPERATION_RESULT_NO_ERRORS, PROCESS_AAI_EVENT,
        String.valueOf(stopTime - startTime));

    setResponse(exchange, ResponseType.SUCCESS, additionalInfo);
    return;
  }

  public List<String> extractSuggestableAttr(VersionedOxmEntities oxmEntities, String entityType) {
    // Extract suggestable attributeshandleTopographicalData
    Map<String, OxmEntityDescriptor> rootDescriptor = oxmEntities.getSuggestableEntityDescriptors();

    if (rootDescriptor == null) {
      return Collections.emptyList();
    }

    OxmEntityDescriptor desc = rootDescriptor.get(entityType);

    if (desc == null) {
      return Collections.emptyList();
    }

    return desc.getSuggestableAttributes();
  }

  public List<String> extractAliasForSuggestableEntity(VersionedOxmEntities oxmEntities,
      String entityType) {

    // Extract alias
    Map<String, OxmEntityDescriptor> rootDescriptor = oxmEntities.getEntityAliasDescriptors();

    if (rootDescriptor == null) {
      return Collections.emptyList();
    }

    OxmEntityDescriptor desc = rootDescriptor.get(entityType);
    return desc.getAlias();
  }

  private void setResponse(Exchange exchange, ResponseType responseType, String additionalInfo) {

    exchange.getOut().setHeader("ResponseType", responseType.toString());
    exchange.getOut().setBody(additionalInfo);
  }

  public void extractDetailsForAutosuggestion(VersionedOxmEntities oxmEntities, String entityType,
      List<String> suggestableAttr, List<String> alias) {

    // Extract suggestable attributes
    Map<String, OxmEntityDescriptor> rootDescriptor = oxmEntities.getSuggestableEntityDescriptors();

    OxmEntityDescriptor desc = rootDescriptor.get(entityType);
    suggestableAttr = desc.getSuggestableAttributes();

    // Extract alias
    rootDescriptor = oxmEntities.getEntityAliasDescriptors();
    desc = rootDescriptor.get(entityType);
    alias = desc.getAlias();
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


  private UebEventHeader initializeUebEventHeader(String payload) {

    UebEventHeader eventHeader = null;
    ObjectMapper mapper = new ObjectMapper();

    // Make sure that were were actually passed in a valid string.
    if (payload == null || payload.isEmpty()) {
      logger.debug(EntityEventPolicyMsgs.UEB_FAILED_TO_PARSE_PAYLOAD, EVENT_HEADER);
      logger.error(EntityEventPolicyMsgs.UEB_FAILED_TO_PARSE_PAYLOAD, EVENT_HEADER);

      return eventHeader;
    }

    // Marshal the supplied string into a UebEventHeader object.
    try {
      eventHeader = mapper.readValue(payload, UebEventHeader.class);
    } catch (JsonProcessingException e) {
      logger.error(EntityEventPolicyMsgs.UEB_FAILED_UEBEVENTHEADER_CONVERSION, e.toString());
    } catch (Exception e) {
      logger.error(EntityEventPolicyMsgs.UEB_FAILED_UEBEVENTHEADER_CONVERSION, e.toString());
    }

    if (eventHeader != null) {
      logger.debug(EntityEventPolicyMsgs.UEB_EVENT_HEADER_PARSED, eventHeader.toString());
    }

    return eventHeader;

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
      logger.debug(EntityEventPolicyMsgs.FAILED_TO_PARSE_UEB_PAYLOAD, ENTITY_HEADER + " missing",
          payload);
      logger.error(EntityEventPolicyMsgs.FAILED_TO_PARSE_UEB_PAYLOAD, ENTITY_HEADER + " missing",
          "");
    }

    return jsonNode;
  }

  private boolean getSearchTags(AaiEventEntity aaiEventEntity, List<String> searchableAttr,
      String payload, String action) {

    boolean hasSearchableAttr = false;
    for (String searchTagField : searchableAttr) {
      String searchTagValue;
      if (searchTagField.equalsIgnoreCase(aaiEventEntity.getEntityPrimaryKeyName())) {
        searchTagValue = aaiEventEntity.getEntityPrimaryKeyValue();
      } else {
        searchTagValue = this.lookupValueUsingKey(payload, searchTagField);
      }

      if (searchTagValue != null && !searchTagValue.isEmpty()) {
        hasSearchableAttr = true;
        aaiEventEntity.addSearchTagWithKey(searchTagValue, searchTagField);
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

  private List<String> getOxmAttributes(String payload, DynamicJAXBContext oxmJaxbContext,
      String oxmEntityType, String entityType, String fieldName) {

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

  private JSONObject getUebEntity(String payload) {
    JSONObject uebJsonObj;

    try {
      uebJsonObj = new JSONObject(payload);
    } catch (JSONException e) {
      logger.debug(EntityEventPolicyMsgs.DISCARD_EVENT_VERBOSE,
          "Payload has invalid JSON Format", payload);
      logger.error(EntityEventPolicyMsgs.DISCARD_EVENT_NONVERBOSE,
          "Payload has invalid JSON Format");
      return null;
    }

    if (uebJsonObj.has(ENTITY_HEADER)) {
      return uebJsonObj.getJSONObject(ENTITY_HEADER);
    } else {
      logger.debug(EntityEventPolicyMsgs.FAILED_TO_PARSE_UEB_PAYLOAD, ENTITY_HEADER + " missing", payload);
      logger.error(EntityEventPolicyMsgs.FAILED_TO_PARSE_UEB_PAYLOAD, ENTITY_HEADER + " missing");
      return null;
    }
  }

  protected AaiEventEntity getPopulatedEntity(JsonNode entityNode,
      OxmEntityDescriptor resultDescriptor) {
    AaiEventEntity d = new AaiEventEntity();

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

  private void updateCerInEntity(AaiEventEntity aaiEventEntity) {
    try {
      Map<String, List<String>> headers = new HashMap<>();
      headers.put(Headers.FROM_APP_ID, Arrays.asList("Data Router"));
      headers.put(Headers.TRANSACTION_ID, Arrays.asList(MDC.get(MdcContext.MDC_REQUEST_ID)));

      String entityId = aaiEventEntity.getId();
      String jsonPayload;

      // Run the GET to retrieve the ETAG from the search service
      OperationResult storedEntity = searchAgent.getDocument(entitySearchIndex, entityId);

      if (HttpUtil.isHttpResponseClassSuccess(storedEntity.getResultCode())) {
        /*
         * NOTES: aaiEventEntity (ie the nested entity) may contain a subset of properties of
         * the pre-existing object,
         * so all we want to do is update the CER on the pre-existing object (if needed).
         */

        List<String> etag = storedEntity.getHeaders().get(Headers.ETAG);

        if (etag != null && !etag.isEmpty()) {
          headers.put(Headers.IF_MATCH, etag);
        } else {
          logger.error(EntityEventPolicyMsgs.NO_ETAG_AVAILABLE_FAILURE,
                  entitySearchIndex, entityId);
        }

        ArrayList<JsonNode> sourceObject = new ArrayList<>();
        NodeUtils.extractObjectsByKey(
            NodeUtils.convertJsonStrToJsonNode(storedEntity.getResult()),
            "content", sourceObject);

        if (!sourceObject.isEmpty()) {
          JsonNode node = sourceObject.get(0);
          final String sourceCer = NodeUtils.extractFieldValueFromObject(node,
              "crossEntityReferenceValues");
          String newCer = aaiEventEntity.getCrossReferenceEntityValues();
          boolean hasNewCer = true;
          if (sourceCer != null && sourceCer.length() > 0){ // already has CER
            if ( !sourceCer.contains(newCer)){//don't re-add
              newCer = sourceCer + ";" + newCer;
            } else {
              hasNewCer = false;
            }
          }

          if (hasNewCer){
            // Do the PUT with new CER
            ((ObjectNode)node).put("crossEntityReferenceValues", newCer);
            jsonPayload = NodeUtils.convertObjectToJson(node, false);
            searchAgent.putDocument(entitySearchIndex, entityId, jsonPayload, headers);
          }
         }
      } else {

        if (storedEntity.getResultCode() == 404) {
          // entity not found, so attempt to do a PUT
          searchAgent.putDocument(entitySearchIndex, entityId, aaiEventEntity.getAsJson(), headers);
        } else {
          logger.error(EntityEventPolicyMsgs.FAILED_TO_UPDATE_ENTITY_IN_DOCSTORE,
              aaiEventEntity.getId(), "SYNC_ENTITY");
        }
      }
    } catch (IOException e) {
      logger.error(EntityEventPolicyMsgs.FAILED_TO_UPDATE_ENTITY_IN_DOCSTORE,
          aaiEventEntity.getId(), "SYNC_ENTITY");
    }
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
  protected void handleSearchServiceOperation(DocumentStoreDataEntity eventEntity,
                                            String                  action,
                                            String                  index) {
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
            logger.error(EntityEventPolicyMsgs.NO_ETAG_AVAILABLE_FAILURE, index,
                entityId);
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
            logger.error(EntityEventPolicyMsgs.NO_ETAG_AVAILABLE_FAILURE, index,
                entityId);
          }

          /*
           * The Spring-Boot version of the search-data-service rejects the DELETE operation unless
           * we specify a Content-Type.
           */

          headers.put("Content-Type", Arrays.asList(MediaType.APPLICATION_JSON.getMediaType()));

          searchAgent.deleteDocument(index, eventEntity.getId(), headers);
        } else {
          logger.error(EntityEventPolicyMsgs.NO_ETAG_AVAILABLE_FAILURE, index,
              entityId);
        }
      } else {
        logger.error(EntityEventPolicyMsgs.ENTITY_OPERATION_NOT_SUPPORTED, action);
      }
    } catch (IOException e) {
      logger.error(EntityEventPolicyMsgs.FAILED_TO_UPDATE_ENTITY_IN_DOCSTORE, eventEntity.getId(),
          action);
    }
  }

  private void handleTopographicalData(String payload, String action, String entityType,
      String oxmEntityType, DynamicJAXBContext oxmJaxbContext, String entityPrimaryKeyFieldName,
      String entityPrimaryKeyFieldValue) {

    Map<String, String> topoData = new HashMap<>();
    String entityLink;
    List<String> topographicalAttr =
        getOxmAttributes(payload, oxmJaxbContext, oxmEntityType, entityType, "geoProps");
    if (topographicalAttr == null) {
      logger.error(EntityEventPolicyMsgs.DISCARD_UPDATING_TOPOGRAPHY_DATA_NONVERBOSE,
          "Topograhical attribute not found for payload entity type '" + entityType + "'");
      logger.debug(EntityEventPolicyMsgs.DISCARD_UPDATING_TOPOGRAPHY_DATA_VERBOSE,
          "Topograhical attribute not found for payload entity type '" + entityType + "'",
          payload);
    } else {
      entityLink = lookupValueUsingKey(payload, "entity-link");
      for (String topoAttr : topographicalAttr) {
        topoData.put(topoAttr, lookupValueUsingKey(payload, topoAttr));
      }
      updateTopographicalSearchDb(topoData, entityType, action, entityPrimaryKeyFieldName,
          entityPrimaryKeyFieldValue, entityLink);
    }

  }

  private void updateTopographicalSearchDb(Map<String, String> topoData, String entityType,
      String action, String entityPrimaryKeyName, String entityPrimaryKeyValue, String entityLink) {

    TopographicalEntity topoEntity = new TopographicalEntity();
    topoEntity.setEntityPrimaryKeyName(entityPrimaryKeyName);
    topoEntity.setEntityPrimaryKeyValue(entityPrimaryKeyValue);
    topoEntity.setEntityType(entityType);
    topoEntity.setLatitude(topoData.get(TOPO_LAT));
    topoEntity.setLongitude(topoData.get(TOPO_LONG));
    topoEntity.setSelfLink(entityLink);
    try {
      topoEntity.setId(TopographicalEntity.generateUniqueShaDigest(entityType, entityPrimaryKeyName,
          entityPrimaryKeyValue));
    } catch (NoSuchAlgorithmException e) {
      logger.error(EntityEventPolicyMsgs.DISCARD_UPDATING_TOPOGRAPHY_DATA_VERBOSE,
          "Cannot create unique SHA digest for topographical data.");
    }

    this.handleSearchServiceOperation(topoEntity, action, topographicalSearchIndex);
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

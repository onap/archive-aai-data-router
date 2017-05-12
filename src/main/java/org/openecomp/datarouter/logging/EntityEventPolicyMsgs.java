/**
 * ﻿============LICENSE_START=======================================================
 * DataRouter
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property.
 * Copyright © 2017 Amdocs
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 * ECOMP and OpenECOMP are trademarks
 * and service marks of AT&T Intellectual Property.
 */
package org.openecomp.datarouter.logging;

import com.att.eelf.i18n.EELFResourceManager;

import org.openecomp.cl.eelf.LogMessageEnum;

public enum EntityEventPolicyMsgs implements LogMessageEnum {

  // Error Messages
  /**
   * Discarding event. 
   * Arguments:
   *    {0} = reason
   *    {1} = Payload:
   */
  DISCARD_AAI_EVENT_VERBOSE,    
    
  /**
   * Discarding event. 
   * Arguments:
   *    {0} = Reason
   */
  DISCARD_AAI_EVENT_NONVERBOSE,
  
  /**
   * OXM version: {0} is not supported. 
   * Arguments:
   *    {0} = OXM Version
   */
  OXM_VERSION_NOT_SUPPORTED,
 
  /**
   * Failed to parse UEB payload. 
   * Arguments:
   *    {0} 
   *    {1}
   */
  FAILED_TO_PARSE_UEB_PAYLOAD,

  /**
   * Unable to retrieve etag at {0} for entity with id {1}
   * Arguments:
   *    {0} = Resource endpoint.
   *    {1} = Entity id.
   */
  NO_ETAG_AVAILABLE_FAILURE,

  /**
   * Failed to update entity {0} with operation {1}.
   * Arguments:
   *   {0} = Entity 
   *   {1} = Operation
   */
  FAILED_TO_UPDATE_ENTITY_IN_DOCSTORE,

  
  /**
   * Action: {0} is not supported.
   * Argument:
   *    {0} = Operation
   */
  ENTITY_OPERATION_NOT_SUPPORTED,

  /**
   * Arguments:
   * {0} = reason
   */
  DISCARD_UPDATING_SEARCH_SUGGESTION_DATA,
  
  /**
   * Discarding topographical data. Reason: {0}. Payload: {1}  
   * Arguments:
   *    {0} = Reason for discarding data.
   *    {1} = Payload
   */
  DISCARD_UPDATING_TOPOGRAPHY_DATA_VERBOSE,
      
  /**
   * Discarding topographical data. Reason: {0}
   * Arguments:
   *    {0} = Reason for discarding data.
   */
  DISCARD_UPDATING_TOPOGRAPHY_DATA_NONVERBOSE,

  /**
   * Failed to load OXM Model.
   */
  PROCESS_OXM_MODEL_MISSING,

  /**
   * Failed to create Search index {0} due to: {1} 
   * 
   * Arguments:
   *    {0} = Search index
   *    {1} = Error cause
   */
  FAIL_TO_CREATE_SEARCH_INDEX,

  /**
   * Failed to find OXM version in UEB payload. {0}
   * Arguments:
   *    {0} = OXM version.
   */
  FAILED_TO_FIND_OXM_VERSION,

  
  // Info Messages
  
  /**
   * Processing AAI Entity Event Policy: 
   * Arguments:
   *    {0} = Action
   *    {1} = Entity Type
   *    {2} = Payload
   */
  PROCESS_AAI_ENTITY_EVENT_POLICY_VERBOSE,

  /**
   * Processing AAI Entity Event Policy: 
   * Arguments:
   *     {0} = Action
   *     {1} = Entity Type 
   */
  PROCESS_AAI_ENTITY_EVENT_POLICY_NONVERBOSE,
  
  /**
   * Cross Entity Reference synchronization {0}
   * Arguments:
   *    {0} = Error string
   * 
   */
  CROSS_ENTITY_REFERENCE_SYNC,
  
  /**
   * Operation {0} completed in {1} ms with no errors
   * Arguments:
   *    {0} = Operation type
   *    {1} = Time in ms.
   */
  OPERATION_RESULT_NO_ERRORS,
  
  /**
   * Found OXM model: {0}
   * Arguments:
   *    {0} = Key pair.
   */
  PROCESS_OXM_MODEL_FOUND,
  
  /**
   * Successfully created index at {0}
   * 
   * Arguments:
   *    {0} = Index resource endpoint
   */
  SEARCH_INDEX_CREATE_SUCCESS,
  
  /**
   * Entity Event Policy component started. 
   */
  ENTITY_EVENT_POLICY_REGISTERED,
 
  /**
   * Arguments:
   *    {0} = Entity name
   */
  PRIMARY_KEY_NULL_FOR_ENTITY_TYPE,

  /**
   * Arguments: {0} = UEB payload
   */
  UEB_INVALID_PAYLOAD_JSON_FORMAT,
  
  /**
   * Arguments: {0} = Event header
   */
  UEB_FAILED_TO_PARSE_PAYLOAD,
  
  /**
   * Arguments: {0} = Exception
   */
  UEB_FAILED_UEBEVENTHEADER_CONVERSION,

  /**
   * Arguments: {0} = UEB event header
   */
  UEB_EVENT_HEADER_PARSED;
  
  /**
   * Static initializer to ensure the resource bundles for this class are loaded...
   */
  static {
    EELFResourceManager.loadMessageBundle("logging/EntityEventPolicyMsgs");
  }
}

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
package org.onap.aai.datarouter.entity;

import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;

import org.onap.aai.datarouter.util.NodeUtils;

/**
 * Note: AAIEventEntity is a port forward of IndexDocument Has been renamed here to move forward
 * with abstraction of document store technology.
 */
public class AaiEventEntity implements DocumentStoreDataEntity, Serializable {

  private static final long serialVersionUID = -5188479658230319058L;

  protected String entityType;
  protected String entityPrimaryKeyName;
  protected String entityPrimaryKeyValue;
  protected ArrayList<String> searchTagCollection = new ArrayList<>();
  protected ArrayList<String> searchTagIdCollection = new ArrayList<>();
  protected ArrayList<String> crossEntityReferenceCollection = new ArrayList<>();
  protected String lastmodTimestamp;
  protected String link;

  private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  /*
   * Generated fields, leave the settings for junit overrides
   */

  // generated, SHA-256 digest
  protected String id;

  /*
   * generated based on searchTagCollection values
   */
  protected String searchTags;
  protected String searchTagIds;
  protected String crossReferenceEntityValues;


  private static String convertBytesToHexString(byte[] bytesToConvert) {
    StringBuilder hexString = new StringBuilder();
    for (int i = 0; i < bytesToConvert.length; i++) {
      hexString.append(Integer.toHexString(0xFF & bytesToConvert[i]));
    }
    return hexString.toString();
  }

  private static String concatArray(List<String> list, char delimiter) {

    if (list == null || list.isEmpty()) {
      return "";
    }

    StringBuilder result = new StringBuilder(64);

    boolean firstValue = true;

    for (String item : list) {

      if (firstValue) {
        result.append(item);
        firstValue = false;
      } else {
        result.append(delimiter).append(item);
      }

    }

    return result.toString();

  }

  public AaiEventEntity() {
    SimpleDateFormat dateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    String currentFormattedTimeStamp = dateFormat.format(timestamp);
    this.lastmodTimestamp = currentFormattedTimeStamp;
  }

  public void deriveFields() throws NoSuchAlgorithmException {
    this.id = NodeUtils.generateUniqueShaDigest(link);
    this.searchTags = concatArray(searchTagCollection, ';');
    this.searchTagIds = concatArray(searchTagIdCollection, ';');
    this.crossReferenceEntityValues = concatArray(crossEntityReferenceCollection, ';');
  }


  /*
   * (non-Javadoc)
   * 
   * @see org.onap.aai.datarouter.entity.AAIEventEntity#getAsJson()
   */
  @Override
  public String getAsJson() throws IOException {

    JsonObject obj = Json.createObjectBuilder().add("entityType", entityType)
        .add("entityPrimaryKeyValue", entityPrimaryKeyValue).add("searchTagIDs", searchTagIds)
        .add("searchTags", searchTags).add("crossEntityReferenceValues", crossReferenceEntityValues)
        .add("lastmodTimestamp", lastmodTimestamp).add("link", link).build();

    return obj.toString();
  }


  public void addSearchTagWithKey(String searchTag, String key) {
    searchTagIdCollection.add(key);
    searchTagCollection.add(searchTag);
  }

  public void addCrossEntityReferenceValue(String crossEntityReferenceValue) {
    if (!crossEntityReferenceCollection.contains(crossEntityReferenceValue)) {
      crossEntityReferenceCollection.add(crossEntityReferenceValue);
    }
  }

  public String getEntityType() {
    return entityType;
  }

  public String getEntityPrimaryKeyName() {
    return entityPrimaryKeyName;
  }

  public String getEntityPrimaryKeyValue() {
    return entityPrimaryKeyValue;
  }


  /*
   * (non-Javadoc)
   * 
   * @see org.onap.aai.datarouter.entity.AAIEventEntity#getId()
   */
  @Override
  public String getId() {
    return id;
  }

  public ArrayList<String> getSearchTagCollection() {
    return searchTagCollection;
  }

  public String getSearchTags() {
    return searchTags;
  }

  public String getSearchTagIDs() {
    return searchTagIds;
  }

  public void setSearchTagIDs(String searchTagIDs) {
    this.searchTagIds = searchTagIDs;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setSearchTagCollection(ArrayList<String> searchTagCollection) {
    this.searchTagCollection = searchTagCollection;
  }

  public void setSearchTags(String searchTags) {
    this.searchTags = searchTags;
  }

  public ArrayList<String> getSearchTagIdCollection() {
    return searchTagIdCollection;
  }

  public void setSearchTagIdCollection(ArrayList<String> searchTagIdCollection) {
    this.searchTagIdCollection = searchTagIdCollection;
  }

  public String getLastmodTimestamp() {
    return lastmodTimestamp;
  }

  public void setLastmodTimestamp(String lastmodTimestamp) {
    this.lastmodTimestamp = lastmodTimestamp;
  }

  public void setEntityPrimaryKeyName(String entityPrimaryKeyName) {
    this.entityPrimaryKeyName = entityPrimaryKeyName;
  }

  public void setEntityPrimaryKeyValue(String entityPrimaryKeyValue) {
    this.entityPrimaryKeyValue = entityPrimaryKeyValue;
  }
  
  public String getLink() {
    return link;
  }
  
  public void setLink(String link) {
    this.link = link;
  }

  /*
   * public void mergeEntity(AAIEventEntity entityToMergeIn) {
   * 
   * if ( entityToMergeIn == null ) { return; }
   * 
   * if ( !entityToMergeIn.getEntityType().equals( entityType )) { entityType =
   * entityToMergeIn.getEntityType(); }
   * 
   * if ( !entityToMergeIn.getEntityType().equals( entityType )) { entityType =
   * entityToMergeIn.getEntityType(); }
   * 
   * }
   */

  public String getCrossReferenceEntityValues() {
    return crossReferenceEntityValues;
  }

  public void setCrossReferenceEntityValues(String crossReferenceEntityValues) {
    this.crossReferenceEntityValues = crossReferenceEntityValues;
  }

  @Override
  public String toString() {
    return "AAIEventEntity [" + (entityType != null ? "entityType=" + entityType + ", " : "")
        + (entityPrimaryKeyName != null ? "entityPrimaryKeyName=" + entityPrimaryKeyName + ", "
            : "")
        + (entityPrimaryKeyValue != null ? "entityPrimaryKeyValue=" + entityPrimaryKeyValue + ", "
            : "")
        + (searchTagCollection != null ? "searchTagCollection=" + searchTagCollection + ", " : "")
        + (searchTagIdCollection != null ? "searchTagIDCollection=" + searchTagIdCollection + ", "
            : "")
        + (crossEntityReferenceCollection != null
            ? "crossEntityReferenceCollection=" + crossEntityReferenceCollection + ", " : "")
        + "lastmodTimestamp=" + lastmodTimestamp + ", " + (id != null ? "id=" + id + ", " : "")
        + (searchTags != null ? "searchTags=" + searchTags + ", " : "")
        + (searchTagIds != null ? "searchTagIDs=" + searchTagIds + ", " : "")
        + (crossReferenceEntityValues != null
            ? "crossReferenceEntityValues=" + crossReferenceEntityValues : "")
        + "]";
  }

}

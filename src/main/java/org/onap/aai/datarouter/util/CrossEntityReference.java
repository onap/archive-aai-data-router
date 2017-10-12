/**
 * ﻿============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2017 Amdocs
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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */
package org.onap.aai.datarouter.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Processing and entity wrapper for property transposition logic and UEB processing
 * 
 * @author DAVEA
 */
public class CrossEntityReference {

  private String targetEntityType;

  private List<String> attributeNames;

  public CrossEntityReference() {
    this.targetEntityType = null;
    this.attributeNames = new ArrayList<>();
  }

  public String getTargetEntityType() {
    return targetEntityType;
  }

  public void setTargetEntityType(String targetEntityType) {
    this.targetEntityType = targetEntityType;
  }

  public List<String> getAttributeNames() {
    return attributeNames;
  }

  public void setAttributeNames(List<String> attributeNames) {
    this.attributeNames = attributeNames;
  }

  public void addAttributeName(String attributeName) {
    if (!this.attributeNames.contains(attributeName)) {
      this.attributeNames.add(attributeName);
    }
  }

  public void initialize(String crossEntityReferenceValueFromModel) {

    if (crossEntityReferenceValueFromModel == null
        || crossEntityReferenceValueFromModel.length() == 0) {
      // or throw an exception due to failure to initialize
      return;
    }

    String[] tokens = crossEntityReferenceValueFromModel.split(",");

    if (tokens.length >= 2) {
      this.targetEntityType = tokens[0];

      for (int x = 1; x < tokens.length; x++) {
        this.attributeNames.add(tokens[x]);
      }
    } else {
      // throw a CrossEntityReferenceInitializationException??
    }

  }

  @Override
  public String toString() {
    return "CrossEntityReference ["
        + (targetEntityType != null ? "entityType=" + targetEntityType + ", " : "")
        + (attributeNames != null ? "attributeNames=" + attributeNames : "") + "]";
  }

}

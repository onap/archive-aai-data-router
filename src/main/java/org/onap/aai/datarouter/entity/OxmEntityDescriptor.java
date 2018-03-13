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

import java.util.List;

import org.onap.aai.datarouter.util.CrossEntityReference;


public class OxmEntityDescriptor {
	
	private String entityName;

	private List<String> primaryKeyAttributeName;
	
	private List<String> searchableAttributes;
	
	private CrossEntityReference crossEntityReference;
	
	private List<String> alias;
	
	private List<String> suggestableAttributes;
    
    boolean isSuggestableEntity;

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	
	public List<String> getPrimaryKeyAttributeName() {
		return primaryKeyAttributeName;
	}

	public void setPrimaryKeyAttributeName(List<String> primaryKeyAttributeName) {
		this.primaryKeyAttributeName = primaryKeyAttributeName;
	}

	public List<String> getSearchableAttributes() {
		return searchableAttributes;
	}

	public void setSearchableAttributes(List<String> searchableAttributes) {
		this.searchableAttributes = searchableAttributes;
	}
	
	public boolean hasSearchableAttributes() {
	   
	   if ( this.searchableAttributes == null) {
	      return false;
	   }
	   
	   if ( !this.searchableAttributes.isEmpty() ) {
	      return true;
	   }
	   
	   return false;
	   
	}

	public CrossEntityReference getCrossEntityReference() {
		return crossEntityReference;
	}

	public void setCrossEntityReference(CrossEntityReference crossEntityReference) {
		this.crossEntityReference = crossEntityReference;
	}

	public List<String> getAlias() {
    return alias;
  }

  public void setAlias(List<String> alias) {
    this.alias = alias;
  }

  public List<String> getSuggestableAttributes() {
    return suggestableAttributes;
  }

  public void setSuggestableAttributes(List<String> suggestableAttributes) {
    this.suggestableAttributes = suggestableAttributes;
  }

  public boolean isSuggestableEntity() {
    return isSuggestableEntity;
  }

  public void setSuggestableEntity(boolean isSuggestableEntity) {
    this.isSuggestableEntity = isSuggestableEntity;
  }

  @Override
  public String toString() {
    return "OxmEntityDescriptor [entityName=" + entityName + ", primaryKeyAttributeName="
        + primaryKeyAttributeName + ", searchableAttributes=" + searchableAttributes
        + ", crossEntityReference=" + crossEntityReference + ", alias=" + alias
        + ", suggestableAttributes=" + suggestableAttributes + ", isSuggestableEntity="
        + isSuggestableEntity + "]";
  }
}

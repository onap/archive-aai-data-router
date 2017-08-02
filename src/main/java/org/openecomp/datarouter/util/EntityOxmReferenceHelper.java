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
package org.openecomp.datarouter.util;

import java.util.HashMap;

import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;

public class EntityOxmReferenceHelper implements ExternalOxmModelProcessor {

   private static EntityOxmReferenceHelper _instance = null;
   
   private HashMap<Version, VersionedOxmEntities> versionedModels;
   
   protected EntityOxmReferenceHelper() {
      this.versionedModels = new HashMap<Version,VersionedOxmEntities>();
   }
   
   public static EntityOxmReferenceHelper getInstance() {
      if ( _instance == null ) {
         _instance = new EntityOxmReferenceHelper();
      }
      
      return _instance;
   }
   
   
   @Override
   public void onOxmVersionChange(Version version, DynamicJAXBContext context) {
      VersionedOxmEntities oxmEntities = new VersionedOxmEntities();
      oxmEntities.initialize(context);
      this.versionedModels.put(version, oxmEntities);
      
   }
   
   public VersionedOxmEntities getVersionedOxmEntities(Version version){ 
      return this.versionedModels.get(version);
   }

}
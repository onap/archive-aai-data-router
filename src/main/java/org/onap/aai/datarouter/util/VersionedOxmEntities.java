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

import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.oxm.XPathFragment;
import org.eclipse.persistence.internal.oxm.mappings.Descriptor;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.oxm.XMLField;
import org.onap.aai.datarouter.entity.OxmEntityDescriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Builds up a representation of the versioned entities in a way that they can be cross referenced
 * in a data-driven way
 *
 * @author DAVEA
 */
public class VersionedOxmEntities {

    private static final String REST_ROOT_ENTITY = "inventory";

    private HashMap<String, Boolean> crossEntityReferenceContainerLookup = new HashMap<>();
    private HashMap<String, CrossEntityReference> crossEntityReferenceLookup = new HashMap<>();
    private Map<String, DynamicType> entityTypeLookup = new LinkedHashMap<>();
    private Map<String, OxmEntityDescriptor> searchableEntityDescriptors = new HashMap<>();
    private Map<String, OxmEntityDescriptor> suggestableEntityDescriptors = new HashMap<>();
    private Map<String, OxmEntityDescriptor> entityAliasDescriptors = new HashMap<>();


    public void initialize(DynamicJAXBContext context) {
        parseOxmContext(context);
        buildCrossEntityReferenceCollections(REST_ROOT_ENTITY, new HashSet<String>());
        populateSearchableDescriptors(context);
    }

    /**
     * The big goal for these methods is to make the processing as generic and model driven as possible.
     * There are only two exceptions to this rule, at the moment.  I needed to hard-coded the top level REST data
     * model entity type, which is "inventory" for now.   And as this class is heavily focused and coupled towards
     * building a version specific set of lookup structures for the "crossEntityReference" model attribute, it possesses
     * knowledge of that attribute whether it exists or not in the DynamicJAXBContext we are currently analyzing.
     * <p>
     * This method will build two collections:
     * <p>
     * 1)  A list of entity types that can have nested entities containing cross entity reference definitions.
     * The purpose of this collection is a fail-fast test when processing UEB events so we can quickly determine if
     * it is necessary to deeply parse the event looking for cross entity reference attributes which not exist.
     * <p>
     * For example, looking at a service-instance <=> inventory path:
     * <p>
     * inventory (true)
     * -> business (true)
     * -> customers  (true)
     * -> customer  (true)
     * -> service-subscriptions (true)
     * -> service-subscription (CER defined here in the model)   (true)
     * -> service-instances    (false)
     * -> service-instance   (false)
     * <p>
     * Because service-subscription contains a model definition of CER, in the first collection all the types in the
     * tree will indicate that it possesses one or more contained entity types with a cross-entity-reference definition.
     * <p>
     * 2)  A lookup for { entityType => CrossEntityReference } so we can quickly access the model definition of a CER
     * for a specific entity type when we begin extracting parent attributes for transposition into nested child entity
     * types.
     *
     * @param entityType
     * @param checked
     * @return
     */
    protected boolean buildCrossEntityReferenceCollections(String entityType, HashSet<String> checked) {

      /*
       * To short-circuit infinite loops, make sure this entityType hasn't
       * already been checked
       */

        if (checked.contains(entityType)) {
            return false;
        } else {
            checked.add(entityType);
        }

        DynamicType parentType = entityTypeLookup.get(entityType);
        DynamicType childType;
        boolean returnValue = false;

        if (parentType == null) {
            return returnValue;
        }

      /*
       * Check if current descriptor contains the cross-entity-reference
       * attribute. If it does not walk the entity model looking for nested
       * entity types that may contain the reference.
       */

        Map<String, String> properties = parentType.getDescriptor().getProperties();
        if (properties != null) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                if ("crossEntityReference".equalsIgnoreCase(entry.getKey())) {
                    returnValue = true;
                    CrossEntityReference cer = new CrossEntityReference();
                    cer.initialize(entry.getValue());
                    crossEntityReferenceLookup.put(entityType, cer);
                    //System.out.println("entityType = " + entityType + " contains a CER instance = " + returnValue);
                    // return true;
                }
            }
        }

        Vector<DatabaseField> fields = parentType.getDescriptor().getAllFields();

        if (fields != null) {

            XMLField xmlField;
            for (DatabaseField f : fields) {

                if (f instanceof XMLField) {
                    xmlField = (XMLField) f;
                    XPathFragment xpathFragment = xmlField.getXPathFragment();
                    String entityShortName = xpathFragment.getLocalName();

                    childType = entityTypeLookup.get(entityShortName);

                    if (childType != null) {

                        if (!checked.contains(entityShortName)) {

                            if (buildCrossEntityReferenceCollections(entityShortName, checked)) {
                                returnValue = true;
                            }

                        }

                        checked.add(entityShortName);

                    }

                }

            }

        }

        crossEntityReferenceContainerLookup.put(entityType, Boolean.valueOf(returnValue));
        return returnValue;
    }

    private void populateSearchableDescriptors(DynamicJAXBContext oxmContext) {
        List<Descriptor> descriptorsList = oxmContext.getXMLContext().getDescriptors();
        OxmEntityDescriptor newOxmEntity;

        for (Descriptor desc : descriptorsList) {

            DynamicType entity = (DynamicType) oxmContext.getDynamicType(desc.getAlias());

            //LinkedHashMap<String, String> oxmProperties = new LinkedHashMap<String, String>();
            String primaryKeyAttributeNames = null;

            //Not all fields have key attributes
            if (desc.getPrimaryKeyFields() != null) {
                primaryKeyAttributeNames = desc.getPrimaryKeyFields()
                        .toString().replaceAll("/text\\(\\)", "").replaceAll("\\[", "").replaceAll("\\]", "");
            }

            String entityName = desc.getDefaultRootElement();

            Map<String, String> properties = entity.getDescriptor().getProperties();
            if (properties != null) {
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    if ("searchable".equalsIgnoreCase(entry.getKey())) {
                  
                  /*
                   * we can do all the work here, we don't have a create additional collections for 
                   * subsequent passes
                   */
                        newOxmEntity = new OxmEntityDescriptor();
                        newOxmEntity.setEntityName(entityName);
                        newOxmEntity
                                .setPrimaryKeyAttributeName(Arrays.asList(primaryKeyAttributeNames.split(",")));
                        newOxmEntity.setSearchableAttributes(Arrays.asList(entry.getValue().split(",")));
                        searchableEntityDescriptors.put(entityName, newOxmEntity);
                    } else if ("containsSuggestibleProps".equalsIgnoreCase(entry.getKey())) {
                        newOxmEntity = new OxmEntityDescriptor();
                        newOxmEntity.setEntityName(entityName);
                        newOxmEntity.setSuggestableEntity(true);
                        Vector<DatabaseMapping> descriptorMaps = entity.getDescriptor().getMappings();
                        List<String> listOfSuggestableAttributes = new ArrayList<>();

                        for (DatabaseMapping descMap : descriptorMaps) {
                            if (descMap.isAbstractDirectMapping()) {

                                if (descMap.getProperties().get("suggestibleOnSearch") != null) {
                                    String suggestableOnSearchString = String.valueOf(
                                            descMap.getProperties().get("suggestibleOnSearch"));

                                    boolean isSuggestibleOnSearch = Boolean.valueOf(suggestableOnSearchString);

                                    if (isSuggestibleOnSearch) {
                         /* Grab attribute types for suggestion */
                                        String attributeName = descMap.getField().getName()
                                                .replaceAll("/text\\(\\)", "");
                                        listOfSuggestableAttributes.add(attributeName);
                                    }
                                }
                            }
                        }
                        newOxmEntity.setSuggestableAttributes(listOfSuggestableAttributes);
                        suggestableEntityDescriptors.put(entityName, newOxmEntity);
                    } else if ("suggestionAliases".equalsIgnoreCase(entry.getKey())) {
                        newOxmEntity = new OxmEntityDescriptor();
                        newOxmEntity.setEntityName(entityName);
                        newOxmEntity.setAlias(Arrays.asList(entry.getValue().split(",")));
                        entityAliasDescriptors.put(entityName, newOxmEntity);
                    }
                }
            }

        }

    }

    public Map<String, OxmEntityDescriptor> getSearchableEntityDescriptors() {
        return searchableEntityDescriptors;
    }

    public OxmEntityDescriptor getSearchableEntityDescriptor(String entityType) {
        return searchableEntityDescriptors.get(entityType);
    }


    public HashMap<String, Boolean> getCrossEntityReferenceContainers() {
        return crossEntityReferenceContainerLookup;
    }

    public HashMap<String, CrossEntityReference> getCrossEntityReferences() {
        return crossEntityReferenceLookup;
    }


    private void parseOxmContext(DynamicJAXBContext oxmContext) {
        List<Descriptor> descriptorsList = oxmContext.getXMLContext().getDescriptors();

        for (Descriptor desc : descriptorsList) {

            DynamicType entity = (DynamicType) oxmContext.getDynamicType(desc.getAlias());

            String entityName = desc.getDefaultRootElement();

            entityTypeLookup.put(entityName, entity);

        }

    }

    public boolean entityModelContainsCrossEntityReference(String containerEntityType) {
        Boolean v = crossEntityReferenceContainerLookup.get(containerEntityType);

        if (v == null) {
            return false;
        }

        return v;
    }

    public boolean entityContainsCrossEntityReference(String entityType) {
        return crossEntityReferenceLookup.get(entityType) != null;
    }

    public CrossEntityReference getCrossEntityReference(String entityType) {
        return crossEntityReferenceLookup.get(entityType);
    }

    public Map<String, OxmEntityDescriptor> getSuggestableEntityDescriptors() {
        return suggestableEntityDescriptors;
    }

    public void setSuggestableEntityDescriptors(
            Map<String, OxmEntityDescriptor> suggestableEntityDescriptors) {
        this.suggestableEntityDescriptors = suggestableEntityDescriptors;
    }

    public Map<String, OxmEntityDescriptor> getEntityAliasDescriptors() {
        return entityAliasDescriptors;
    }

    public void setEntityAliasDescriptors(Map<String, OxmEntityDescriptor> entityAliasDescriptors) {
        this.entityAliasDescriptors = entityAliasDescriptors;
    }

    public void extractEntities(String entityType, DynamicJAXBContext context, Collection<DynamicType> entities) {


    }

    public String dumpCrossEntityReferenceContainers() {

        Set<String> keys = crossEntityReferenceContainerLookup.keySet();
        StringBuilder sb = new StringBuilder(128);

        for (String key : keys) {

            if (crossEntityReferenceContainerLookup.get(key)) {
                sb.append("\n").append("Entity-Type = '" + key + "' contains a Cross-Entity-Reference.");
            }
        }


        return sb.toString();

    }

}

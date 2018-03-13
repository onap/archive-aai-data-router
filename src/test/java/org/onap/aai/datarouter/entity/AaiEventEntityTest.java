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

import org.junit.Assert;
import org.junit.Test;
import org.onap.aai.datarouter.util.CrossEntityReference;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AaiEventEntityTest {

    @Test
    public void testAddCrossEntityReferenceValue(){
        AaiEventEntity eventEntity = new AaiEventEntity();
        Assert.assertTrue(eventEntity.crossEntityReferenceCollection.size()==0);
        eventEntity.addCrossEntityReferenceValue("reference-1");
        Assert.assertTrue(eventEntity.crossEntityReferenceCollection.size()==1);
    }

    @Test
    public void testOtherMethods(){
        AaiEventEntity eventEntity = new AaiEventEntity();
        eventEntity.setEntityType("type-1");
        Assert.assertEquals(eventEntity.getEntityType(), "type-1");

        eventEntity.setSearchTagCollection(new ArrayList<String>());
        Assert.assertTrue(eventEntity.getSearchTagCollection().size()==0);

        eventEntity.setSearchTags("tag-1");
        Assert.assertEquals(eventEntity.getSearchTags(), "tag-1");

        eventEntity.setSearchTagIDs("tagid-1");
        Assert.assertEquals(eventEntity.getSearchTagIDs(), "tagid-1");

        eventEntity.setId("id-1");
        Assert.assertEquals(eventEntity.getId(), "id-1");

        eventEntity.setSearchTagIdCollection(new ArrayList<String>());
        Assert.assertTrue(eventEntity.getSearchTagIdCollection().size()==0);

        eventEntity.setLastmodTimestamp("2017-28-12");
        Assert.assertEquals(eventEntity.getLastmodTimestamp(), "2017-28-12");

        eventEntity.setLink("link-1");
        Assert.assertEquals(eventEntity.getLink(), "link-1");

        eventEntity.setCrossReferenceEntityValues("cross-entity");
        Assert.assertEquals(eventEntity.getCrossReferenceEntityValues(), "cross-entity");

        Assert.assertNotNull(eventEntity.toString());
    }

    @Test
    public void testAggregationEntity(){
        AggregationEntity entity = new AggregationEntity();
        entity.setLastmodTimestamp("2017-28-12");
        Assert.assertEquals(entity.getLastmodTimestamp(), "2017-28-12");

        entity.addAttributeKeyValuePair("key1", "value1");

        Map<String, Object> entityMap = new HashMap<String, Object>();
        entityMap.put("relationship", "relationship-value");
        entityMap.put("relationship-list", new ArrayList<String>());
        entity.copyAttributeKeyValuePair(entityMap);

        Assert.assertTrue(entity.attributes.containsKey("relationship"));
        Assert.assertFalse(entity.attributes.containsKey("relationship-list"));

        entity.setId("id-1");
        entity.setLink("link-1");
        Assert.assertNotNull(entity.toString());
    }

    @Test
    public void testOxmEntityDescriptor(){
        OxmEntityDescriptor descriptor = new OxmEntityDescriptor();
        descriptor.setEntityName("entity-1");
        Assert.assertEquals(descriptor.getEntityName(), "entity-1");

        descriptor.setPrimaryKeyAttributeName(new ArrayList<String>());
        Assert.assertTrue(descriptor.getPrimaryKeyAttributeName().size()==0);

        Assert.assertFalse(descriptor.hasSearchableAttributes());

        List<String> searchableAttr = new ArrayList<String>();
        searchableAttr.add("search");
        descriptor.setSearchableAttributes(searchableAttr);
        Assert.assertTrue(descriptor.getSearchableAttributes().size()==1);

        Assert.assertTrue(descriptor.hasSearchableAttributes());

        CrossEntityReference ref = new CrossEntityReference();
        descriptor.setCrossEntityReference(ref);
        Assert.assertEquals(descriptor.getCrossEntityReference(), ref);

        descriptor.setSuggestableEntity(true);
        Assert.assertTrue(descriptor.isSuggestableEntity());

        Assert.assertNotNull(descriptor.toString());
    }

    @Test
    public void testPolicyResponse(){
        PolicyResponse response = new PolicyResponse(PolicyResponse.ResponseType.SUCCESS, "response-data");

        Assert.assertEquals(response.getResponseType(), PolicyResponse.ResponseType.SUCCESS);
        Assert.assertEquals(response.getResponseData(), "response-data");

        response.setHttpResponseCode(200);
        Assert.assertTrue(response.getHttpResponseCode() == 200);

        Assert.assertNotNull(response.toString());
    }

    @Test
    public void testTopographicalEntity() throws NoSuchAlgorithmException, IOException {
        TopographicalEntity entity = new TopographicalEntity();
        String retStr = entity.generateUniqueShaDigest("entity", "name-1", "value-1");
        Assert.assertNotNull(retStr);

        entity.setEntityType("type-1");
        Assert.assertEquals(entity.getEntityType(),"type-1");
        entity.setEntityPrimaryKeyName("key-1");
        Assert.assertEquals(entity.getEntityPrimaryKeyName(), "key-1");
        entity.setEntityPrimaryKeyValue("value-1");
        Assert.assertEquals(entity.getEntityPrimaryKeyValue(),"value-1");
        entity.setLatitude("37.0902");
        Assert.assertEquals(entity.getLatitude(),"37.0902");
        entity.setLongitude("95.7129");
        Assert.assertEquals(entity.getLongitude(),"95.7129");
        entity.setId("id-1");
        Assert.assertEquals(entity.getId(),"id-1");
        entity.setSelfLink("link-1");
        Assert.assertEquals(entity.getSelfLink(),"link-1");

        Assert.assertNotNull(TopographicalEntity.getSerialversionuid());
        Assert.assertNotNull(entity.getAsJson());
        Assert.assertNotNull(entity.toString());
    }
}

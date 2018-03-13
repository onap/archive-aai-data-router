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
package org.onap.aai.datarouter.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CrossEntityReferenceTest {

    @Test
    public void testCrossEntityReference() {

        CrossEntityReference reference = new CrossEntityReference();

        reference.setTargetEntityType("entity-type");
        Assert.assertEquals(reference.getTargetEntityType(),"entity-type");

        reference.setAttributeNames(new ArrayList<String>());
        Assert.assertTrue(reference.getAttributeNames().size()==0);

        reference.addAttributeName("attribute");
        Assert.assertEquals(reference.getAttributeNames().get(0),"attribute");
    }


}
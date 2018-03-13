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
package org.onap.aai.datarouter.exception;

import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;

public class DataRouterExceptionTest {

    @Test
    public void testDataRouterError(){
        DataRouterError error1 = DataRouterError.DL_PARSE_100;
        Assert.assertEquals("DL-100", error1.getId());
        Assert.assertNotNull(error1.getMessage());
        Assert.assertEquals(Response.Status.BAD_REQUEST, error1.getHttpStatus());
    }

    @Test
    public void testBaseDataRouterException(){
        BaseDataRouterException exp1 = new BaseDataRouterException("id-1");
        Assert.assertEquals(exp1.getId(), "id-1");

        BaseDataRouterException exp2 = new BaseDataRouterException("id-1", "test-error");
        Assert.assertEquals(exp2.getId(), "id-1");

        BaseDataRouterException exp3 = new BaseDataRouterException("id-1", "test-error", new Throwable());
        Assert.assertEquals(exp3.getId(), "id-1");
    }
}

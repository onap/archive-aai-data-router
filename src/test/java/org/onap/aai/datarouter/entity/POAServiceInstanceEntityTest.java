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

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response.Status;
import org.junit.Assert;
import org.junit.Test;
import org.onap.aai.datarouter.exception.POAAuditException;

public class POAServiceInstanceEntityTest {

	@Test
	public void testPOAServiceInstanceEntity(){
		String svcInstanceId = "24602405-7714-4c64-81da-9e182a3eba59";
		String modelVersionId = "2f836857-d399-4de3-a6f8-e4a09d3017eb";
		String modelInvariantId = "8c383ba3-20c3-4196-b092-c8c007ef7ddc";
		String  customerId =  "global-customer-01";
		String  serviceType = "vFW";
		String xFromAppId ="REST-client";
		String xTransactionId = "aaa111cccc4444";

		POAServiceInstanceEntity  svcEntity= new POAServiceInstanceEntity();
		svcEntity.setServiceInstanceId(svcInstanceId);
		svcEntity.setModelVersionId(modelVersionId);
		svcEntity.setModelInvariantId(modelInvariantId);
		svcEntity.setCustomerId(customerId);
		svcEntity.setServiceType(serviceType);
		svcEntity.setxFromAppId(xFromAppId);
		svcEntity.setxTransactionId(xTransactionId);

		Assert.assertEquals(svcInstanceId, svcEntity.getServiceInstanceId());
		Assert.assertEquals(modelVersionId, svcEntity.getModelVersionId());
		Assert.assertEquals(modelInvariantId, svcEntity.getModelInvariantId());

		Assert.assertEquals(customerId, svcEntity.getCustomerId());
		Assert.assertEquals(serviceType, svcEntity.getServiceType());
		Assert.assertEquals(xFromAppId, svcEntity.getxFromAppId());
		Assert.assertEquals(xTransactionId, svcEntity.getxTransactionId());

	}

	@Test
	public void testNullServiceInstanceId() throws POAAuditException {
		String modelVersionId = "2f836857-d399-4de3-a6f8-e4a09d3017eb";
		String modelInvariantId = "8c383ba3-20c3-4196-b092-c8c007ef7ddc";
		String  customerId =  "global-customer-01";
		String  serviceType = "vFW";

		POAServiceInstanceEntity  svcEntity= new POAServiceInstanceEntity();
		svcEntity.setServiceInstanceId(null);
		svcEntity.setModelVersionId(modelVersionId);
		svcEntity.setModelInvariantId(modelInvariantId);
		svcEntity.setCustomerId(customerId);
		svcEntity.setServiceType(serviceType);

		try  {
			svcEntity.validate();
		} catch (POAAuditException e) {
			assertEquals(Status.BAD_REQUEST,  e.getHttpStatus());
		}
	}


	@Test
	public void testEmptyServiceInstanceId() throws POAAuditException {
		String modelVersionId = "2f836857-d399-4de3-a6f8-e4a09d3017eb";
		String modelInvariantId = "8c383ba3-20c3-4196-b092-c8c007ef7ddc";
		String  customerId =  "global-customer-01";
		String  serviceType = "vFW";

		POAServiceInstanceEntity  svcEntity= new POAServiceInstanceEntity();
		svcEntity.setServiceInstanceId("");
		svcEntity.setModelVersionId(modelVersionId);
		svcEntity.setModelInvariantId(modelInvariantId);
		svcEntity.setCustomerId(customerId);
		svcEntity.setServiceType(serviceType);

		try  {
			svcEntity.validate();
		} catch (POAAuditException e) {
			assertEquals(Status.BAD_REQUEST,  e.getHttpStatus());
		}
	}


	@Test
	public void testNullModelVersionId() throws POAAuditException {
		String svcInstanceId = "24602405-7714-4c64-81da-9e182a3eba59";
		String modelInvariantId = "8c383ba3-20c3-4196-b092-c8c007ef7ddc";
		String  customerId =  "global-customer-01";
		String  serviceType = "vFW";

		POAServiceInstanceEntity  svcEntity= new POAServiceInstanceEntity();
		svcEntity.setServiceInstanceId(svcInstanceId);
		svcEntity.setModelVersionId(null);
		svcEntity.setModelInvariantId(modelInvariantId);
		svcEntity.setCustomerId(customerId);
		svcEntity.setServiceType(serviceType);

		try  {
			svcEntity.validate();
		} catch (POAAuditException e) {
			assertEquals(Status.BAD_REQUEST,  e.getHttpStatus());
		}
	}

	@Test
	public void testEmptyModelVersionId() throws POAAuditException {
		String svcInstanceId = "24602405-7714-4c64-81da-9e182a3eba59";
		String modelInvariantId = "8c383ba3-20c3-4196-b092-c8c007ef7ddc";
		String  customerId =  "global-customer-01";
		String  serviceType = "vFW";

		POAServiceInstanceEntity  svcEntity= new POAServiceInstanceEntity();
		svcEntity.setServiceInstanceId(svcInstanceId);
		svcEntity.setModelVersionId("");
		svcEntity.setModelInvariantId(modelInvariantId);
		svcEntity.setCustomerId(customerId);
		svcEntity.setServiceType(serviceType);

		try  {
			svcEntity.validate();
		} catch (POAAuditException e) {
			assertEquals(Status.BAD_REQUEST,  e.getHttpStatus());
		}
	}


	@Test
	public void testNullModelInvariantId() throws POAAuditException {
		String svcInstanceId = "24602405-7714-4c64-81da-9e182a3eba59";
		String modelVersionId = "2f836857-d399-4de3-a6f8-e4a09d3017eb";
		String  customerId =  "global-customer-01";
		String  serviceType = "vFW";

		POAServiceInstanceEntity  svcEntity= new POAServiceInstanceEntity();
		svcEntity.setServiceInstanceId(svcInstanceId);
		svcEntity.setModelVersionId(modelVersionId);
		svcEntity.setModelInvariantId(null);
		svcEntity.setCustomerId(customerId);
		svcEntity.setServiceType(serviceType);

		try  {
			svcEntity.validate();
		} catch (POAAuditException e) {
			assertEquals(Status.BAD_REQUEST,  e.getHttpStatus());
		}
	}


	@Test
	public void testEmptyModelInvariantId() throws POAAuditException {
		String svcInstanceId = "24602405-7714-4c64-81da-9e182a3eba59";
		String modelVersionId = "2f836857-d399-4de3-a6f8-e4a09d3017eb";
		String  customerId =  "global-customer-01";
		String  serviceType = "vFW";

		POAServiceInstanceEntity  svcEntity= new POAServiceInstanceEntity();
		svcEntity.setServiceInstanceId(svcInstanceId);
		svcEntity.setModelVersionId(modelVersionId);
		svcEntity.setModelInvariantId("");
		svcEntity.setCustomerId(customerId);
		svcEntity.setServiceType(serviceType);

		try  {
			svcEntity.validate();
		} catch (POAAuditException e) {
			assertEquals(Status.BAD_REQUEST,  e.getHttpStatus());
		}
	}

	@Test
	public void testNullServiceType() throws POAAuditException {
		String svcInstanceId = "24602405-7714-4c64-81da-9e182a3eba59";
		String modelVersionId = "2f836857-d399-4de3-a6f8-e4a09d3017eb";
		String modelInvariantId = "8c383ba3-20c3-4196-b092-c8c007ef7ddc";
		String  customerId =  "global-customer-01";


		POAServiceInstanceEntity  svcEntity= new POAServiceInstanceEntity();
		svcEntity.setServiceInstanceId(svcInstanceId);
		svcEntity.setModelVersionId(modelVersionId);
		svcEntity.setModelInvariantId(modelInvariantId);
		svcEntity.setCustomerId(customerId);
		svcEntity.setServiceType(null);

		try  {
			svcEntity.validate();
		} catch (POAAuditException e) {
			assertEquals(Status.BAD_REQUEST,  e.getHttpStatus());
		}
	}


	@Test
	public void testEmptyServiceType() throws POAAuditException {
		String svcInstanceId = "24602405-7714-4c64-81da-9e182a3eba59";
		String modelVersionId = "2f836857-d399-4de3-a6f8-e4a09d3017eb";
		String modelInvariantId = "8c383ba3-20c3-4196-b092-c8c007ef7ddc";
		String  customerId =  "global-customer-01";


		POAServiceInstanceEntity  svcEntity= new POAServiceInstanceEntity();
		svcEntity.setServiceInstanceId(svcInstanceId);
		svcEntity.setModelVersionId(modelVersionId);
		svcEntity.setModelInvariantId(modelInvariantId);
		svcEntity.setCustomerId(customerId);
		svcEntity.setServiceType("");

		try  {
			svcEntity.validate();
		} catch (POAAuditException e) {
			assertEquals(Status.BAD_REQUEST,  e.getHttpStatus());
		}
	}


	@Test
	public void testNullCustomerId() throws POAAuditException {
		String svcInstanceId = "24602405-7714-4c64-81da-9e182a3eba59";
		String modelVersionId = "2f836857-d399-4de3-a6f8-e4a09d3017eb";
		String modelInvariantId = "8c383ba3-20c3-4196-b092-c8c007ef7ddc";
  	    String  serviceType = "vFW";

		POAServiceInstanceEntity  svcEntity= new POAServiceInstanceEntity();
		svcEntity.setServiceInstanceId(svcInstanceId);
		svcEntity.setModelVersionId(modelVersionId);
		svcEntity.setModelInvariantId(modelInvariantId);
		svcEntity.setCustomerId(null);
		svcEntity.setServiceType(serviceType);

		try  {
			svcEntity.validate();
		} catch (POAAuditException e) {
			assertEquals(Status.BAD_REQUEST,  e.getHttpStatus());
		}
	}


	@Test
	public void testEmptyCustomerId() throws POAAuditException {
		String svcInstanceId = "24602405-7714-4c64-81da-9e182a3eba59";
		String modelVersionId = "2f836857-d399-4de3-a6f8-e4a09d3017eb";
		String modelInvariantId = "8c383ba3-20c3-4196-b092-c8c007ef7ddc";
  	    String  serviceType = "vFW";

		POAServiceInstanceEntity  svcEntity= new POAServiceInstanceEntity();
		svcEntity.setServiceInstanceId(svcInstanceId);
		svcEntity.setModelVersionId(modelVersionId);
		svcEntity.setModelInvariantId(modelInvariantId);
		svcEntity.setCustomerId("");
		svcEntity.setServiceType(serviceType);

		try  {
			svcEntity.validate();
		} catch (POAAuditException e) {
			assertEquals(Status.BAD_REQUEST,  e.getHttpStatus());
		}
	}

}

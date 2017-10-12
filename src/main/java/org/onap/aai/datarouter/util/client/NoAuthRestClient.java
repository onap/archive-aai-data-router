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
package org.onap.aai.datarouter.util.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.cl.mdc.MdcContext;
import org.onap.aai.datarouter.logging.DataRouterMsgs;
import org.onap.aai.datarouter.util.AaiUiSvcPolicyUtil;
import org.onap.aai.datarouter.util.NodeUtils;
import org.onap.aai.datarouter.util.RouterServiceUtil;
import org.onap.aai.restclient.client.Headers;
import org.onap.aai.restclient.client.OperationResult;
import org.onap.aai.restclient.client.RestClient;
import org.onap.aai.restclient.enums.RestAuthenticationMode;
import org.onap.aai.restclient.rest.HttpUtil;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoAuthRestClient implements SvcRoutingRestClient {

    private RestClient restClient;

    private String host;
    private String port;
    private String originUrl;
    private String targetUri;
    private JsonNode targetPayload;
    private Logger logger;
    private Logger auditLogger;

    public NoAuthRestClient(int connectTimeOut, int readTimeOut) {
        LoggerFactory loggerFactoryInstance = LoggerFactory.getInstance();
        logger = loggerFactoryInstance.getLogger(NoAuthRestClient.class.getName());
        auditLogger = loggerFactoryInstance.getAuditLogger(NoAuthRestClient.class.getName());
        restClient = new RestClient().authenticationMode(RestAuthenticationMode.HTTP_NOAUTH)
                .connectTimeoutMs(connectTimeOut).readTimeoutMs(readTimeOut);
    }


    private OperationResult getResults(String url, JsonNode payload) {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(Headers.FROM_APP_ID, Arrays.asList("Synapse"));
        headers.put(Headers.TRANSACTION_ID, Arrays.asList(MDC.get(MdcContext.MDC_REQUEST_ID)));
        return this.getRestClient().post(url, payload.asText(),
                                         headers, MediaType.APPLICATION_JSON_TYPE,
                                         MediaType.APPLICATION_JSON_TYPE);
    }

    public final void handleRequest(String host, String port, Exchange exchange) throws Exception {
        RouterServiceUtil.setMdcContext(exchange);
        Message message = exchange.getIn();
        String body = message.getBody(String.class);
        OperationResult result = new OperationResult();

        this.setHost(host);
        this.setPort(port);

        this.setOriginUrl(message.getHeader(Exchange.HTTP_URL).toString());
        if (body != null && body.length() != 0) {
            JsonNode node = NodeUtils.convertJsonStrToJsonNode(body);
            this.setTargetPayload(AaiUiSvcPolicyUtil.getOriginPayload(node));
            this.setTargetUri(AaiUiSvcPolicyUtil.getTargetUri(node));
        }

        if (this.getTargetPayload() == null || this.getTargetUri() == null) {
            logger.error(DataRouterMsgs.INVALID_ORIGIN_PAYLOAD, body);
            result.setResultCode(HttpStatus.BAD_REQUEST.value());
            result.setFailureCause("Invalid payload");
        }

        String targetUrl = "http://" + host + ":" + port + "/" + this.targetUri;
        auditLogger.info(DataRouterMsgs.ROUTING_FROM_TO, this.getOriginUrl(), targetUrl);
        long startTimeInMs = System.currentTimeMillis();

        result = this.getResults(targetUrl, targetPayload);

        long targetMsOpTime = System.currentTimeMillis() - startTimeInMs;
        auditLogger.info(DataRouterMsgs.OP_TIME, "Target service at " + targetUrl, String.valueOf(targetMsOpTime));

        int rc = result.getResultCode();
        String resultStr;
        if (HttpUtil.isHttpResponseClassSuccess(rc)) {
            resultStr = result.getResult();
        } else {
            resultStr = result.getFailureCause();
        }

        logger.debug(DataRouterMsgs.ROUTING_RESPONSE, targetUrl, result.toString());
        exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, rc);
        exchange.getOut().setBody(resultStr);
    }


    /* (non-Javadoc)
     * @see org.onap.aai.datarouter.util.client.SvcRoutingRestClient#getHost()
     */
    @Override
    public String getHost() {
        return host;
    }

    /* (non-Javadoc)
     * @see org.onap.aai.datarouter.util.client.SvcRoutingRestClient#setHost(java.lang.String)
     */
    @Override
    public void setHost(String host) {
        this.host = host;
    }


    /* (non-Javadoc)
     * @see org.onap.aai.datarouter.util.client.SvcRoutingRestClient#getPort()
     */
    @Override
    public String getPort() {
        return port;
    }


    /* (non-Javadoc)
     * @see org.onap.aai.datarouter.util.client.SvcRoutingRestClient#setPort(java.lang.String)
     */
    @Override
    public void setPort(String port) {
        this.port = port;
    }


    /* (non-Javadoc)
     * @see org.onap.aai.datarouter.util.client.SvcRoutingRestClient#getTargetUri()
     */
    @Override
    public String getTargetUri() {
        return targetUri;
    }


    /* (non-Javadoc)
     * @see org.onap.aai.datarouter.util.client.SvcRoutingRestClient#setTargetUri(java.lang.String)
     */
    @Override
    public void setTargetUri(String targetUri) {
        this.targetUri = targetUri;
    }


    /* (non-Javadoc)
     * @see org.onap.aai.datarouter.util.client.SvcRoutingRestClient#getTargetPayload()
     */
    @Override
    public JsonNode getTargetPayload() {
        return targetPayload;
    }


    /* (non-Javadoc)
     * @see org.onap.aai.datarouter.util.client
     * .SvcRoutingRestClient#setTargetPayload(com.fasterxml.jackson.databind.JsonNode)
     */
    @Override
    public void setTargetPayload(JsonNode targetPayload) {
        this.targetPayload = targetPayload;
    }


    /* (non-Javadoc)
     * @see org.onap.aai.datarouter.util.client.SvcRoutingRestClient#getRestClient()
     */
    @Override
    public RestClient getRestClient() {
        return restClient;
    }

    /* (non-Javadoc)
     * @see org.onap.aai.datarouter.util.client.SvcRoutingRestClient#setRestClient()
     */
    @Override
    public void setRestClient(RestClient client) {
        this.restClient = client;
    }


    public String getOriginUrl() {
        return originUrl;
    }


    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

}

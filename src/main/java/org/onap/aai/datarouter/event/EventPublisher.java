/**
 * ============LICENSE_START=======================================================
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
package org.onap.aai.datarouter.event;

import java.util.List;
import javax.ws.rs.core.Response.Status;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.datarouter.exception.POAAuditException;
import org.onap.aai.datarouter.logging.DataRouterMsgs;
import org.onap.aai.datarouter.util.DataRouterProperties;
import org.onap.aai.event.client.DMaaPEventPublisher;

/**
 * Publish the POMBA event to DMAAP
 *
 */
public class EventPublisher {

    private static Logger logger = LoggerFactory.getInstance().getLogger(EventPublisher.class.getName());

    private static String OPERATION = "POST to DMaaP";
    private static int MAX_BATCH_SIZE = 100;
    private static int MAX_AGE_MS = 250;
    private static int DEFAULT_BATCH_DELAY = 50;

    private static String DEFAULT_HOST = "localhost";
    private static String DEFAULT_PORT = "3904";
    private static Long   DEFAULT_TIMEOUT_MS = 20000L;
    private static String DEFAULT_TOPIC = "POA-AUDIT-INIT";
    private static String DEFAULT_PARTITION = "1";
    private static String DEFAULT_TRANSPORT_TYPE = "HTTPAUTH";
    private static String DEFAULT_USER_NAME = "";
    private static String DEFAULT_PASSWORD = "";

    private String dmaapHost = DEFAULT_HOST + ":" + DEFAULT_PORT;
    private Long timeout = DEFAULT_TIMEOUT_MS;
    private String topicName = DEFAULT_TOPIC;
    private String partition = DEFAULT_PARTITION;
    private String transportType = DEFAULT_TRANSPORT_TYPE;
    private String userName = "";
    private String userPassword = "";


    private enum DMaaPConfig {
        HOST("host"),
        PORT("port"),
        TIMEOUT("timeout-ms"),
        TRANSPORT_TYPE("transport-type"),
        TOPIC_NAME("topic.name"),
        PARTITION("publisher.partition"),
        USER_NAME("user.name"),
        PASSWORD("user.password");

        private String prefix = "data-router.dmaap.";
        private String key;

        DMaaPConfig(String key) {
            this.key = prefix + key;
        }

        public String key() {
            return this.key;
        }
    }



    public EventPublisher() {
        String host = DataRouterProperties.get(DMaaPConfig.HOST.key(), DEFAULT_HOST);
        String port = DataRouterProperties.get(DMaaPConfig.PORT.key(), DEFAULT_PORT);
        dmaapHost = host + ":" + port;

        try {
            timeout = Long.parseLong(DataRouterProperties.get(DMaaPConfig.TIMEOUT.key(), Long.toString(DEFAULT_TIMEOUT_MS)));
        } catch(NumberFormatException e) {
            logger.warn(DataRouterMsgs.WARN_USING_DEFAULT, DMaaPConfig.TIMEOUT.key(), Long.toString(DEFAULT_TIMEOUT_MS));
        }

        topicName = DataRouterProperties.get(DMaaPConfig.TOPIC_NAME.key(), DEFAULT_TOPIC);
        transportType = DataRouterProperties.get(DMaaPConfig.TRANSPORT_TYPE.key(), DEFAULT_TRANSPORT_TYPE);
        partition = DataRouterProperties.get(DMaaPConfig.PARTITION.key(), DEFAULT_PARTITION);
        userName = DataRouterProperties.get(DMaaPConfig.USER_NAME.key(), DEFAULT_USER_NAME);
        userPassword = DataRouterProperties.get(DMaaPConfig.PASSWORD.key(), DEFAULT_PASSWORD);
    }


    /**
     * Publish events to DMaaP.
     * @param eventMessages
     * @throws POAAuditException
     *
     * Note: The Default Transport Type in the DMaaPEventPublisher is defined as "HTTPAAF". Based on the deployment of DMaap, currently
     * by default the "HTTPAUTH" transport type is supported.
     */
    public void send(List<String> eventMessages) throws POAAuditException {

        DMaaPEventPublisher publisher = new DMaaPEventPublisher(dmaapHost, topicName, userName, userPassword, MAX_BATCH_SIZE, MAX_AGE_MS, DEFAULT_BATCH_DELAY, transportType);

        int messagesSent = 0;
        List<String> unsentMessages = null;
        try {
            messagesSent = publisher.sendSync(partition, eventMessages);
            logger.debug("The number of messages successfully sent: "+ messagesSent);
            unsentMessages = publisher.closeWithUnsent();
        } catch (Exception e) {
            throw new POAAuditException("Failed to publish event to DMaaP: " + e.getMessage(), Status.INTERNAL_SERVER_ERROR,
                    DataRouterMsgs.EXCEPTION_DURING_METHOD_CALL, OPERATION, generateUri(), e.getMessage());
        }


        if(unsentMessages.size() > 0) {
            String exceptionStr = String.format("Failed to publish %d of %d event(s) to DMaaP", unsentMessages.size(), eventMessages.size());
            logger.debug(exceptionStr);
            for(String unsent : unsentMessages) {
                logger.debug("Unpublished event: " + unsent);
            }
            throw new POAAuditException(exceptionStr, Status.INTERNAL_SERVER_ERROR,
                    DataRouterMsgs.FAIL_TO_SEND_TO_DMAAP, Integer.toString(unsentMessages.size()), "details in debug log");
        }

    }



    private String generateUri() {
        return "http://" + dmaapHost + "/events/" + topicName;
    }
}

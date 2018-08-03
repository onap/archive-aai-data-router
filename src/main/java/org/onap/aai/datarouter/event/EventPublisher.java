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

import com.att.nsa.cambria.client.CambriaBatchingPublisher;
import com.att.nsa.cambria.client.CambriaClientBuilders;
import com.att.nsa.cambria.client.CambriaPublisher;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.Response.Status;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.datarouter.exception.POAAuditException;
import org.onap.aai.datarouter.logging.DataRouterMsgs;
import org.onap.aai.datarouter.util.DataRouterProperties;

/**
 * Publish the POMBA event to DMAAP
 *
 */
public class EventPublisher {

    private static Logger logger = LoggerFactory.getInstance().getLogger(EventPublisher.class.getName());

    private static String OPERATION = "POST to DMaaP";
    private static int MAX_BATCH_SIZE = 100;
    private static int MAX_AGE_MS = 250;
    private static boolean ENABLE_COMPRESSION = false;

    private static String DEFAULT_HOST = "localhost";
    private static String DEFAULT_PORT = "3904";
    private static Long   DEFAULT_TIMEOUT_MS = 20000L;
    private static String DEFAULT_TOPIC = "ORCHESTRATION_COMPLETED";
    private static String DEFAULT_PARTITION = "1";
    private static String DEFAULT_TRANSPORT_TYPE = "HTTPAUTH";
    private static String DEFAULT_USER_NAME = "";
    private static String DEFAULT_PASSWORD = "";

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

    private String dmaapHost = DEFAULT_HOST + ":" + DEFAULT_PORT;
    private Long timeout = DEFAULT_TIMEOUT_MS;
    private String topicName = DEFAULT_TOPIC;
    private String partition = DEFAULT_PARTITION;
    private String transportType = DEFAULT_TRANSPORT_TYPE;
    private String userName = "";
    private String userPassword = "";


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
     * @return Returns number of events published
     * @throws POAAuditException
     */
    public int send(List<String> eventMessages) throws POAAuditException {

        CambriaBatchingPublisher publisher = createPublisher();

        int messagesSent = 0;
        List<CambriaPublisher.message> unsentMessages = null;
        try {
            messagesSent = publisher.send(convertToPublisherMessage(eventMessages));
            unsentMessages = publisher.close(timeout, TimeUnit.MILLISECONDS);
        } catch(IOException e) {
            throw new POAAuditException("Error occured when publishing event(s) to DMaaP: " + e.getMessage(), Status.INTERNAL_SERVER_ERROR,
                    DataRouterMsgs.EXCEPTION_DURING_METHOD_CALL, OPERATION, generateUri(), e.getMessage());
        } catch (InterruptedException e) {
            throw new POAAuditException("Failed to publish event to DMaaP: " + e.getMessage(), Status.INTERNAL_SERVER_ERROR,
                    DataRouterMsgs.EXCEPTION_DURING_METHOD_CALL, OPERATION, generateUri(), e.getMessage());
        }

        if(unsentMessages.size() > 0) {
            String exceptionStr = String.format("Failed to publish %d of %d event(s) to DMaaP", unsentMessages.size(), eventMessages.size());
            logger.debug(exceptionStr);
            for(CambriaPublisher.message unsent : unsentMessages) {
                logger.debug("Unpublished event: " + unsent.fMsg);
            }
            throw new POAAuditException(exceptionStr, Status.INTERNAL_SERVER_ERROR,
                    DataRouterMsgs.FAIL_TO_SEND_TO_DMAAP, Integer.toString(unsentMessages.size()), "details in debug log");
        }
        return messagesSent;
    }


    /**
     * Creates a list of publisher messages for the configure partition
     * @param eventMessages
     * @return
     */
    private List<CambriaPublisher.message> convertToPublisherMessage(List<String> eventMessages) {

        List<CambriaPublisher.message> publisherMessages = new ArrayList<CambriaPublisher.message>();
        for(String eventMsg : eventMessages) {
            publisherMessages.add(new CambriaPublisher.message(partition, eventMsg));
        }
        return publisherMessages;
    }


    /**
     * Creates an event publisher
     * @return
     * @throws POAAuditException
     */
    private CambriaBatchingPublisher createPublisher() throws POAAuditException {

        CambriaBatchingPublisher publisher = null;
        try {
            if(!userName.isEmpty() && !userPassword.isEmpty()) {
                logger.debug("Publisher using authentication: " + userName + "/" + userPassword);
                publisher = new CambriaClientBuilders.PublisherBuilder()
                        .usingHosts(dmaapHost)
                        .onTopic(topicName)
                        .limitBatch(MAX_BATCH_SIZE, MAX_AGE_MS)
                        .enableCompresion(ENABLE_COMPRESSION)
                        .authenticatedBy(userName, userPassword)
                        .build();
            } else {
                publisher = new CambriaClientBuilders.PublisherBuilder()
                        .usingHosts(dmaapHost)
                        .onTopic(topicName)
                        .limitBatch(MAX_BATCH_SIZE, MAX_AGE_MS)
                        .enableCompresion(ENABLE_COMPRESSION)
                        .build();
            }
        } catch (MalformedURLException e) {
            throw new POAAuditException("Failed to to create event publisher: " + e.getMessage(), Status.INTERNAL_SERVER_ERROR,
                    DataRouterMsgs.EXCEPTION_DURING_METHOD_CALL, OPERATION, generateUri(), e.getMessage());
        } catch (GeneralSecurityException e) {
            throw new POAAuditException("Failed to to create event publisher: " + e.getMessage(), Status.INTERNAL_SERVER_ERROR,
                    DataRouterMsgs.EXCEPTION_DURING_METHOD_CALL, OPERATION, generateUri(), e.getMessage());
        }
        return publisher;
    }


    private String generateUri() {
        return "http://" + dmaapHost + "/events/" + topicName;
    }
}

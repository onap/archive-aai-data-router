package org.onap.aai.datarouter.util;

import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.datarouter.logging.EntityEventPolicyMsgs;

import java.util.Iterator;
import java.util.Map;

public class OxmUtil {

    private OxmUtil() {

    }

    /**
     * Returns latest oxm version.
     * @param oxmVersionContextMap the OxmVersionContextMap
     * @param logger the Logger
     * @return latest oxm version
     */
    public static String parseLatestOxmVersion(Map<String, DynamicJAXBContext> oxmVersionContextMap, Logger logger) {
        int latestVersion = -1;
        String oxmVersion = null;
        if (oxmVersionContextMap != null) {
            Iterator it = oxmVersionContextMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String version = pair.getKey().toString();
                int versionNum = Integer.parseInt(version.substring(1, version.length()));
                if (versionNum > latestVersion) {
                    latestVersion = versionNum;
                    oxmVersion = pair.getKey().toString();
                }
                logger.info(EntityEventPolicyMsgs.PROCESS_OXM_MODEL_FOUND, pair.getKey().toString());
            }
        } else {
            logger.error(EntityEventPolicyMsgs.PROCESS_OXM_MODEL_MISSING, "");
        }
        return oxmVersion;
    }
}

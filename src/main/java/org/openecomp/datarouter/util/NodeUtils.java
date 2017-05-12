/**
 * ﻿============LICENSE_START=======================================================
 * DataRouter
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property.
 * Copyright © 2017 Amdocs
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 * ECOMP and OpenECOMP are trademarks
 * and service marks of AT&T Intellectual Property.
 */
package org.openecomp.datarouter.util;

import java.util.Arrays;

public class NodeUtils {
  /**
  * Generate unique sha digest. This method is copy over from NodeUtils class in AAIUI
  *
  * @param keys the keys
  * @return the string
  */
  public static String generateUniqueShaDigest(String... keys) {
    if ((keys == null) || keys.length == 0) {
      return null;
    }
  
    final String keysStr = Arrays.asList(keys).toString();
    final String hashedId = org.apache.commons.codec.digest.DigestUtils.sha256Hex(keysStr);
  
    return hashedId;
  }
}

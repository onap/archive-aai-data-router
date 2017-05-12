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

import java.util.ArrayList;
import java.util.List;

public class SearchSuggestionPermutation {
  /*
   * Will return all the unique combinations of the suggestions provided.
   * The order of the permutation is not taken into account when computing
   * the uniqueness.
   * e.g.: A list of A,B,C,D will return
   * [[A], [A, B, C, D], [A, C, D], [A, D], [B], [B, C, D], [B, D], [C], [C, D], [D]]
   * 
   * @param list The list of statuses to create permutations of
   * @return     A list which contains a array list of all possible combinations
   */
  @SuppressWarnings("serial")
  public List<ArrayList<String>> getSuggestionsPermutation(List<String> list) {
    List<String> statusList = new ArrayList<>(list);
    List<String> dupStatusList;
    ArrayList<ArrayList<String>> uniqueList = new ArrayList<>();
    int mainLoopIndexCounter = 0;

    for (String status : statusList) {
      // Add the single entity subset
      //This will add the unique single values eg [A],[B],[C],[D]
      uniqueList.add(new ArrayList<String>() {
        {
          add(status);
        }
      });

      // Remove all the elements to left till the current index
      dupStatusList = truncateListUntill(statusList, mainLoopIndexCounter);

      while (!dupStatusList.isEmpty()) {
        ArrayList<String> suggListInIterate= new ArrayList<>();
        suggListInIterate.add(status);

        for (String dupStatus : dupStatusList) {
          suggListInIterate.add(dupStatus);
        }

        uniqueList.add(suggListInIterate);
        dupStatusList.remove(0);
      }

      mainLoopIndexCounter++;
    }

    return uniqueList;
  }

  private List<String> truncateListUntill(List<String> lists, int index) {
    List<String> truncatedList = new ArrayList<>(lists);
    int counter = 0;

    while (counter <= index) {
      truncatedList.remove(0);
      counter++;
    }

    return truncatedList;
  }
}

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
  public static ArrayList<ArrayList<String>> getUniqueListForSuggestions(
      List<String> originalList) {
    ArrayList<ArrayList<String>> lists = new ArrayList<>();
    if (originalList.isEmpty()) {
      lists.add(new ArrayList<String>());
      return lists;
    }
    List<String> list = new ArrayList<>(originalList);
    String head = list.get(0);
    ArrayList<String> rest = new ArrayList<>(list.subList(1, list.size()));
    
    for (ArrayList<String> activeList : getUniqueListForSuggestions(rest)) {
      ArrayList<String> newList = new ArrayList<>();
      newList.add(head);
      newList.addAll(activeList);
      lists.add(newList);
      lists.add(activeList);
    }
    return lists;
  }
  
  public static ArrayList<ArrayList<String>> getNonEmptyUniqueLists(List<String> list){
    ArrayList<ArrayList<String>> lists = getUniqueListForSuggestions(list);
    // remove empty list from the power set 
    for (ArrayList<String> emptyList : lists ){
      if ( emptyList.isEmpty() ) {
        lists.remove(emptyList);
        break;
      }
    }
    return lists;
  }

  public static List<List<String>> getListPermutations(List<String> list) {
    List<String> inputList = new ArrayList<>();
    inputList.addAll(list);
    if (!inputList.isEmpty()) {
      List<List<String>> result = new ArrayList<>();
      result.add(new ArrayList<String>());
      return result;
    }

    List<List<String>> listOfLists = new ArrayList<>();

    String firstElement = inputList.remove(0);

    List<List<String>> recursiveReturn = getListPermutations(inputList);
    for (List<String> li : recursiveReturn) {

      for (int index = 0; index <= li.size(); index++) {
        List<String> temp = new ArrayList<>(li);
        temp.add(index, firstElement);
        listOfLists.add(temp);
      }

    }
    return listOfLists;
  }

}

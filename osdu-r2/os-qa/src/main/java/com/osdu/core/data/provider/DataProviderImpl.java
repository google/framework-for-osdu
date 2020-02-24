/*
 * Copyright  2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.osdu.core.data.provider;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;

import java.io.FileReader;
import java.util.*;

public class DataProviderImpl {
    static JsonElement element;
    static List<Map<String, String>> parseFromJson;

    /**
     * Fills in the list with the data from JsonElement
     */
    public static void fillInList(String filePath, String blockName) {
        parse(filePath, blockName);
        parseFromJson = new Gson().fromJson(element, new TypeToken<List<Map<String, String>>>() {
        }.getType());
    }

    /**
     * Convert List<Map> into List<Object> so it can be used for data provider
     *
     * @return iterator
     */
    public static Iterator<Object[]> mainIterator() {
        List<Object[]> data = new ArrayList<>();
        parseFromJson.forEach(item -> data.add(new Object[]{new HashMap<>(item)}));
        return data.iterator();
    }

    /**
     * Parse json data into JsonElement
     *
     * @param filepath  for json
     * @param blockName in the json file
     */
    @SneakyThrows
    private static void parse(String filepath, String blockName) {
        element = new JsonParser().parse(new FileReader(filepath))
                .getAsJsonObject()
                .get(blockName);
    }
}
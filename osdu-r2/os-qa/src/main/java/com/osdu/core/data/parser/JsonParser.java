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

package com.osdu.core.data.parser;

import com.osdu.core.reporter.TestReporter;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class JsonParser {
    static FileReader reader;
    static Object parsedJson;

    public static Object readJson(String filename) {
        TestReporter.reportDebugStep("Try to get access to file: %s", filename);
        try {
            reader = new FileReader(filename);
        } catch (FileNotFoundException exception) {
            TestReporter.reportErrorStep("File not found: %s", exception);
        }

        TestReporter.reportDebugStep("Successfully read file: %s", filename);
        JSONParser jsonParser = new JSONParser();
        try {
            parsedJson = jsonParser.parse(reader);
        } catch (IOException | ParseException exception) {
            TestReporter.reportErrorStep("File can't be parsed: %s", exception);
        }
        return parsedJson;
    }
}
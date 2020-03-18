// Copyright 2017-2019, Schlumberger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.core.common.logging;

import lombok.extern.java.Log;

import org.opengroup.osdu.core.common.model.http.Request;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.logging.Level;

@Log
@Component
public class DefaultLogWriter implements ILogWriter {

    public void writeJsonEntry(String logname, Map<String, Object> json, Map<String, String> labels) {
        log.log(Level.INFO, String.format("%s %s", json, labels));
    }
    public void writeRequestEntry(String logname, String text, Request request, Map<String, String> labels) {
        log.log(Level.INFO, String.format("%s %s %s", text, request, labels));
    }
    public void writeEntry(String logname, Level severity, String text, Map<String, String> labels){
        log.log(severity, String.format("%s: %s %s", logname, text, labels));
    }

    @Override
    public void close()  {
        //do nothing
    }
}

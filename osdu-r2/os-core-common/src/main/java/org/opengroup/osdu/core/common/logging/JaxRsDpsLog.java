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

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.http.HeadersToLog;
import org.opengroup.osdu.core.common.model.http.Request;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequestScope
public class JaxRsDpsLog implements AutoCloseable {

	@Value("${LOG_PREFIX}")
    private String LOG_PREFIX;

	private ILogger log;
	private DpsHeaders headers;

	@Inject
	public JaxRsDpsLog(ILogger log, DpsHeaders headers){
		this.log = log;
		this.headers = headers;
	}

	public void audit(AuditPayload auditPayload) {
		log.audit(LOG_PREFIX + ".audit", auditPayload, this.getLabels());
	}

	public void request(Request httpRequest) {
		log.request(LOG_PREFIX + ".request", httpRequest, this.getLabels());
	}

	public void info(String message) {
		log.info(LOG_PREFIX + ".app", message, this.getLabels());
	}

	public void warning(String message) {
		log.warning(LOG_PREFIX + ".app", message, this.getLabels());
	}

	public void warning(List<String> messages) {
		if (messages == null || messages.isEmpty()) {
			return;
		}
		int sn = 0;
		StringBuilder sb = new StringBuilder();
		for (String s : messages) {
			sb.append(String.format("%d: %s", sn++, s)).append(System.lineSeparator());
		}
		log.warning(LOG_PREFIX + ".app", sb.toString(), this.getLabels());
	}

	public void warning(String message, Exception e) {
		log.warning(LOG_PREFIX + ".app", message, e, this.getLabels());
	}

	public void error(String message) {
		log.error(LOG_PREFIX + ".app", message, this.getLabels());
	}

	public void error(String message, Exception e) {
		log.error(LOG_PREFIX + ".app", message, e, this.getLabels());
	}

	@Override
	public void close() throws Exception {
	}

	private Map<String, String> getLabels() {
		return new HeadersToLog(Collections.emptyList()).createStandardLabelsFromMap(this.headers.getHeaders());
	}
}
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

package org.opengroup.osdu.core.common.http;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.storage.MessageContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequestScope
public class RequestBodyExtractor {
	private static final String INVALID_PUBSUB_MESSAGE = "Invalid pubsub message";
	private static final Gson GSON = new Gson();
	private MessageContent messageContent;

	@Autowired
	private HttpServletRequest httpServletRequest;

	public Map<String, String> extractAttributesFromRequestBody() {
		if (this.messageContent == null) {
			this.messageContent = this.extractPubsubMessageFromRequestBody();
		}
		return this.messageContent.getAttributes();
	}

	public String extractDataFromRequestBody() {
		if (this.messageContent == null) {
			this.messageContent = this.extractPubsubMessageFromRequestBody();
		}
		return this.messageContent.getData();
	}

	private MessageContent extractPubsubMessageFromRequestBody() {
		try {
			JsonParser jsonParser = new JsonParser();
			BufferedReader reader = this.httpServletRequest.getReader();
			Stream<String> lines = reader.lines();
			String requestBody = lines.collect(Collectors.joining("\n"));
			JsonElement jsonRoot = jsonParser.parse(requestBody);
			if (!(jsonRoot instanceof JsonObject)) {
				throw new AppException(HttpStatus.SC_BAD_REQUEST, "RequestBody is not JsonObject.",
						"Request Body should be JsonObject to be processed.");
			}
			JsonElement message = jsonRoot.getAsJsonObject().get("message");
			if (message == null) {
				throw new AppException(HttpStatus.SC_BAD_REQUEST, INVALID_PUBSUB_MESSAGE, "message object not found");
			}
			MessageContent content = GSON.fromJson(message.toString(), MessageContent.class);

			Map<String, String> attributes = content.getAttributes();
			if (attributes.isEmpty()) {
				throw new AppException(HttpStatus.SC_BAD_REQUEST, INVALID_PUBSUB_MESSAGE, "attribute map not found");
			}
			Map<String, String> lowerCase = new HashMap<>();
			attributes.forEach((key, value) -> lowerCase.put(key.toLowerCase(), value));
			if (Strings.isNullOrEmpty(attributes.get(DpsHeaders.ACCOUNT_ID))
					&& Strings.isNullOrEmpty(attributes.get(DpsHeaders.DATA_PARTITION_ID))) {
				throw new AppException(HttpStatus.SC_BAD_REQUEST, INVALID_PUBSUB_MESSAGE,
						"No tenant information from pubsub message.");
			}
			content.setAttributes(lowerCase);

			String decoded = new String(Base64.getDecoder().decode(content.getData()));
			content.setData(decoded);

			return content;
		} catch (IOException e) {
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Request payload parsing error",
					"Unable to parse request payload.", e);
		}
	}
}

// Copyright 2017-2019, Schlumberger
// Copyright 2020 Google LLC
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

package org.opengroup.osdu.core.common.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.jsonwebtoken.lang.Collections;
import org.apache.commons.lang3.ArrayUtils;
import org.opengroup.osdu.core.common.model.entitlements.Acl;
import org.opengroup.osdu.core.common.model.legal.Legal;
import org.opengroup.osdu.core.common.model.storage.RecordAncestry;
import org.opengroup.osdu.core.common.model.storage.RecordMetadata;

import java.util.ArrayList;
import java.util.List;

public class PersistenceHelper {

	private static final String DATA_PROPERTY = "data.";

	private PersistenceHelper() {
		// private constructor
	}

	public static JsonElement filterRecordDataFields(JsonElement record, List<String> attributes) {

		if (attributes == null || attributes.isEmpty()) {
			return record;
		}

		JsonObject recordJsonObject = record.getAsJsonObject();

		JsonObject dataElement = recordJsonObject.get("data").getAsJsonObject();

		JsonObject filteredData = new JsonObject();

		for (String attribute : attributes) {

			JsonElement property = getDataSubProperty(attribute, dataElement);
			if (property != null) {
				filteredData.add(attribute, property);
			}
		}

		// Replace the data content
		recordJsonObject.add("data", filteredData);

		return recordJsonObject;
	}

	public static List<String> getValidRecordAttributes(String[] attributes) {

		List<String> validAttributes = new ArrayList<>();

		if (ArrayUtils.isEmpty(attributes)) {
			return validAttributes;
		}

		for (String a : attributes) {
			String attribute = a.trim();
			if (attribute.startsWith(DATA_PROPERTY)) {
				validAttributes.add(attribute.replace(DATA_PROPERTY, ""));
			}
		}

		return validAttributes;
	}

	public static String combineRecordMetaDataAndRecordData(JsonElement jsonRecord, RecordMetadata recordMetadata,
			Long version) {
		JsonObject jsonRecordObject = jsonRecord.getAsJsonObject();
		jsonRecordObject.addProperty("id", recordMetadata.getId());
		jsonRecordObject.addProperty("version", version);
		jsonRecordObject.addProperty("kind", recordMetadata.getKind());

		Gson gson = new GsonBuilder().create();
		JsonElement json = gson.toJsonTree(recordMetadata.getAcl(), Acl.class);
		jsonRecordObject.add("acl", json);

		json = gson.toJsonTree(recordMetadata.getLegal(), Legal.class);
		jsonRecordObject.add("legal", json);

		if (recordMetadata.getAncestry() != null && !Collections.isEmpty(recordMetadata.getAncestry().getParents())) {
			json = gson.toJsonTree(recordMetadata.getAncestry(), RecordAncestry.class);
			jsonRecordObject.add("ancestry", json);
		}

		return jsonRecordObject.toString();
	}

	public static JsonObject combineRecordMetaDataAndRecordDataIntoJsonObject(JsonElement jsonRecord,
			RecordMetadata recordMetadata,
			Long version) {
		JsonObject jsonRecordObject = jsonRecord.getAsJsonObject();
		jsonRecordObject.addProperty("id", recordMetadata.getId());
		jsonRecordObject.addProperty("version", version);
		jsonRecordObject.addProperty("kind", recordMetadata.getKind());

		Gson gson = new GsonBuilder().create();
		JsonElement json = gson.toJsonTree(recordMetadata.getAcl(), Acl.class);
		jsonRecordObject.add("acl", json);

		json = gson.toJsonTree(recordMetadata.getLegal(), Legal.class);
		jsonRecordObject.add("legal", json);

		if (recordMetadata.getAncestry() != null && !Collections.isEmpty(recordMetadata.getAncestry().getParents())) {
			json = gson.toJsonTree(recordMetadata.getAncestry(), RecordAncestry.class);
			jsonRecordObject.add("ancestry", json);
		}

		return jsonRecordObject;
	}

	private static JsonElement getDataSubProperty(String field, JsonObject data) {

		if (field.contains(".")) {
			String[] fieldArray = field.split("\\.", 2);
			String subFieldParent = fieldArray[0];
			String subFieldChild = fieldArray[1];

			JsonElement subFieldParentElement = data.get(subFieldParent);

			if (subFieldParentElement.isJsonObject()) {
				JsonElement parentObjectValue = getDataSubProperty(subFieldChild,
						subFieldParentElement.getAsJsonObject());

				if (parentObjectValue != null) {
					return parentObjectValue;
				}
			}

			return null;
		}

		return data.get(field);
	}
}

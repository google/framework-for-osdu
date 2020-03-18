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

package org.opengroup.osdu.core.common.model.storage;

public final class StorageRole {

	private StorageRole() {
	}

	public static final String PREFIX = "ROLE_";

	public static final String VIEWER = "service.storage.viewer";
	public static final String CREATOR = "service.storage.creator";
	public static final String ADMIN = "service.storage.admin";
	public static final String PUBSUB = "storage.pubsub";

	public static final String ROLE_VIEWER = PREFIX + "service.storage.viewer";
	public static final String ROLE_CREATOR = PREFIX + "service.storage.creator";
	public static final String ROLE_ADMIN = PREFIX + "service.storage.admin";
	public static final String ROLE_PUBSUB = PREFIX + "storage.pubsub";
}
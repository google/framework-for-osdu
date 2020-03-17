/*
 * Copyright 2020 Google LLC
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

package org.opengroup.osdu.core.common.model.crs;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@Component
public class CrsPropertySet {
	private Set<String> xPropertyNames;
	private Set<String> yPropertyNames;
	private Set<String> zPropertyNames;
	private Set<String> nestedPropertyNames;
	private Map<String, String> propertyPairing;

	public Set<String> getxPropertyNames() {
		if (this.xPropertyNames == null) {
			this.xPropertyNames = new HashSet<>();
		}

		this.xPropertyNames.add("X");
		this.xPropertyNames.add("LON");
		this.xPropertyNames.add("Longitude");
		this.xPropertyNames.add("wlbEwUtm");
		this.xPropertyNames.add("wlbEwDesDeg");
		this.xPropertyNames.add("TOPHOLEXNG");
		this.xPropertyNames.add("TOPHOLEXDD");
		this.xPropertyNames.add("BHLongitude");
		this.xPropertyNames.add("Utm_X");

		return this.xPropertyNames;
	}

	public Set<String> getyPropertyNames() {
		if (this.yPropertyNames == null) {
			this.yPropertyNames = new HashSet<>();
		}

		this.yPropertyNames.add("Y");
		this.yPropertyNames.add("LAT");
		this.yPropertyNames.add("Latitude");
		this.yPropertyNames.add("wlbNsUtm");
		this.yPropertyNames.add("wlbNsDecDeg");
		this.yPropertyNames.add("TOPHOLEYNG");
		this.yPropertyNames.add("TOPHOLEYDD");
		this.yPropertyNames.add("BHLatitude");
		this.yPropertyNames.add("Utm_Y");

		return this.yPropertyNames;
	}

	public Set<String> getzPropertyNames() {
		if (this.zPropertyNames == null) {
			this.zPropertyNames = new HashSet<>();
		}

		this.zPropertyNames.add("Z");

		return this.zPropertyNames;
	}

	public Set<String> getNestedPropertyNames() {
		if (this.nestedPropertyNames == null) {
			this.nestedPropertyNames = new HashSet<>();
		}

		this.nestedPropertyNames.add("projectOutlineLocalGeographic");
		this.nestedPropertyNames.add("projectOutlineProjected");

		return this.nestedPropertyNames;
	}

	public Map<String, String> getPropertyPairing() {
		if (this.propertyPairing == null) {
			this.propertyPairing = new HashMap<>();
		}

		this.propertyPairing.put("x", "y");
		this.propertyPairing.put("lon", "lat");
		this.propertyPairing.put("long", "lat");
		this.propertyPairing.put("longitude", "latitude");
		this.propertyPairing.put("wlbewutm", "wlbnsutm");
		this.propertyPairing.put("wlbewdesdeg", "wlbnsdecdeg");
		this.propertyPairing.put("topholexng", "topholeyng");
		this.propertyPairing.put("topholexdd", "topholeydd");
		this.propertyPairing.put("bhlongitude", "bhlatitude");
		this.propertyPairing.put("utm_x", "utm_y");

		return this.propertyPairing;
	}
}

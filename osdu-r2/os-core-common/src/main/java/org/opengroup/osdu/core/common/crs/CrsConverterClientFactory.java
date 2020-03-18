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

package org.opengroup.osdu.core.common.crs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

@Component
public class CrsConverterClientFactory extends AbstractFactoryBean<ICrsConverterFactory> {

    //TODO: make it private once endpoint is up for all clouds
	@Value("${CRS_API:}")
	public String CRS_API;

	@Override
	protected ICrsConverterFactory createInstance() throws Exception {
		return new CrsConverterFactory(CrsConverterAPIConfig
				.builder()
				.rootUrl(CRS_API)
				.build());
	}

	@Override
	public Class<?> getObjectType() {
		return ICrsConverterFactory.class;
	}
}

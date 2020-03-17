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

package org.opengroup.osdu.core.common.legal;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.http.HttpClient;

public class LegalFactory implements ILegalFactory {

    private final LegalAPIConfig config;

    public LegalFactory(LegalAPIConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("LegalAPIConfig cannot be empty");
        }
        this.config = config;
    }

    @Override
    public ILegalProvider create(DpsHeaders headers) {
        if (headers == null) {
            throw new NullPointerException("headers cannot be null");
        }
        return new LegalService(this.config, new HttpClient(), headers);
    }
}

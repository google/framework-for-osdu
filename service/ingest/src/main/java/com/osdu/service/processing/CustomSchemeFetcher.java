/*
 * Copyright 2019 Google LLC
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

package com.osdu.service.processing;

import com.networknt.schema.uri.URIFetcher;
import com.osdu.exception.IngestException;
import com.osdu.model.dto.SchemaDataDto;
import com.osdu.repository.SchemaDataRepository;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomSchemeFetcher implements URIFetcher {

  final SchemaDataRepository schemaDataRepository;

  public InputStream fetch(final URI uri) {

    SchemaDataDto schemaData = schemaDataRepository.findByReference(uri.toString());
    if (schemaData == null) {
      throw new IngestException(String.format("Not found validation schema for reference %s", uri));
    }

    return new ByteArrayInputStream(schemaData.getSchema().getBytes(StandardCharsets.UTF_8));
  }
}

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

package com.osdu.service.helper;

import static java.lang.String.format;

import com.osdu.exception.IngestException;
import com.osdu.model.ResourceTypeId;
import com.osdu.model.delfi.Acl;
import com.osdu.model.type.manifest.ManifestFile;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class IngestionHelper {

  /**
   * Generate SRN like "srn:{type_id}:{uuid}:{1 as version}".
   * @param resourceTypeId resource type id
   * @return generated SRN with 1st version
   */
  public static String generateSrn(ResourceTypeId resourceTypeId) {
    String uuid = UUID.randomUUID().toString().replace("-", "");
    return String.format("srn:%s:%s:1", resourceTypeId.getType(), uuid);
  }

  /**
   * Put 1st version if type ID has no version.
   * @param resourceTypeId resource type ID
   * @return prepared resource type ID with version
   */
  public static String prepareTypeId(String resourceTypeId) {
    ResourceTypeId typeId = new ResourceTypeId(resourceTypeId);
    return typeId.hasVersion() ? resourceTypeId : resourceTypeId + "1";
  }

  /**
   * Build Acl from group email map.
   */
  public static Acl getAcl(Map<String, String> groupEmailByName) {
    return Acl.builder()
        .owner(groupEmailByName.get("data.default.owners"))
        .viewer(groupEmailByName.get("data.default.viewers"))
        .build();
  }

  /**
   * Creates URL from ManifestFile proper field.
   */
  public URL createUrlFromManifestFile(ManifestFile file) {
    String preLoadFilePath = file.getData().getGroupTypeProperties().getPreLoadFilePath();
    try {
      return new URL(preLoadFilePath);
    } catch (MalformedURLException e) {
      throw new IngestException(
          format("Could not create URL from staging link : %s", preLoadFilePath),
          e);
    }
  }

  /**
   * Returns file name from URL. Is used to get file name from signed URL.
   */
  public String getFileNameFromUrl(URL fileUrl) {
    try {
      final String fileName = Paths.get(new URI(fileUrl.toString()).getPath()).getFileName()
          .toString();
      if (StringUtils.isEmpty(fileName)) {
        throw new IngestException(format("File name obtained is empty, URL : %s", fileUrl));
      }
      return fileName;
    } catch (URISyntaxException e) {
      throw new IngestException(format("Can not get file name from URL: %s", fileUrl), e);
    }
  }

}

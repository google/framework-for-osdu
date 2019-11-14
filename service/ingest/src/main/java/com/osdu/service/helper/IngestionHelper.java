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

import static com.osdu.service.JsonUtils.toObject;
import static java.lang.String.format;

import com.fasterxml.jackson.core.type.TypeReference;
import com.osdu.exception.IngestException;
import com.osdu.model.ResourceTypeId;
import com.osdu.model.delfi.Acl;
import com.osdu.model.type.manifest.LoadManifest;
import com.osdu.model.type.manifest.ManifestFile;
import com.osdu.model.type.manifest.ManifestWpc;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class IngestionHelper {

  private static final Pattern PARTITION_PATTERN = Pattern.compile("[^a-zA-Z0-9]+");

  public static String generateSrn(ResourceTypeId resourceTypeId) {
    String uuid = UUID.randomUUID().toString().replace("-", "");
    return String.format("srn:%s:%s:1", resourceTypeId.getType(), uuid);
  }

  public static String normalizePartition(String partition) {
    return RegExUtils.replaceAll(partition, PARTITION_PATTERN, "");
  }

  public static List<String> getResourceHostRegionIDs(String resourceHostRegionIDs) {
    return Optional.ofNullable(resourceHostRegionIDs)
        .map(regionIDs -> toObject(resourceHostRegionIDs, new TypeReference<List<String>>() {}))
        .orElse(Collections.emptyList());
  }

  public List<ManifestWpc> getWorkProductComponents(LoadManifest loadManifest) {
    Map<String, ManifestFile> fileById = loadManifest.getFiles().stream()
        .collect(Collectors.toMap(ManifestFile::getAssociativeId, Function.identity()));
    return loadManifest.getWorkProductComponents().stream()
        .map(wpc -> {
          wpc.setFiles(wpc.getFileAssociativeIds().stream()
              .map(fileById::get)
              .map(file -> {
                file.setWpc(wpc);
                return file;
              })
              .collect(Collectors.toList()));
          return wpc;
        })
        .collect(Collectors.toList());
  }

  public static Acl getAcl(Map<String, String> groupEmailByName) {
    return Acl.builder()
        .owner(groupEmailByName.get("data.default.owners"))
        .viewer(groupEmailByName.get("data.default.viewers"))
        .build();
  }

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

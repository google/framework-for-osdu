package com.osdu.service.helper;

import com.osdu.exception.IngestException;
import com.osdu.model.ResourceTypeId;
import com.osdu.model.delfi.Acl;
import com.osdu.model.manifest.LoadManifest;
import com.osdu.model.manifest.ManifestFile;
import com.osdu.model.manifest.WorkProductComponent;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
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
    return StringUtils.removeEnd(resourceTypeId.getRaw(), ":") + uuid + ":";
  }

  public static String normalizePartition(String partition) {
    return RegExUtils.replaceAll(partition, PARTITION_PATTERN, "");
  }

  public List<WorkProductComponent> getWorkProductComponents(LoadManifest loadManifest) {
    Map<String, ManifestFile> fileById = loadManifest.getFiles().stream()
        .collect(Collectors.toMap(ManifestFile::getAssociativeId, Function.identity()));
    return loadManifest.getWorkProductComponents().stream()
        .map(wpc -> wpc.toBuilder()
            .files(wpc.getFileAssociativeIds().stream()
                .map(fileById::get)
                .map(file -> file.toBuilder()
                    .wpc(wpc)
                    .build())
                .collect(Collectors.toList()))
            .build())
        .collect(Collectors.toList());
  }

  public static Acl getAcl(Map<String, String> groupEmailByName) {
    return Acl.builder()
        .owner(groupEmailByName.get("data.default.owners"))
        .viewer(groupEmailByName.get("data.default.viewers"))
        .build();
  }

  public URL createUrlFromManifestFile(ManifestFile file) {
    try {
      return new URL(file.getData().getGroupTypeProperties().getStagingFilePath());
    } catch (MalformedURLException e) {
      throw new IngestException(
          String.format("Could not create URL from staging link : %s",
              file.getData().getGroupTypeProperties().getStagingFilePath()),
          e);
    }
  }

  public String getFileNameFromUrl(URL fileUrl) {
    try {
      final String fileName = Paths.get(new URI(fileUrl.toString()).getPath()).getFileName()
          .toString();
      if (StringUtils.isEmpty(fileName)) {
        throw new IngestException(String.format("File name obtained is empty, URL : %s", fileUrl));
      }
      return fileName;
    } catch (URISyntaxException e) {
      throw new IngestException(String.format("Can not get file name from URL: %s", fileUrl), e);
    }
  }
}

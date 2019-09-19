package com.osdu.service.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.ByteStreams;
import com.google.api.client.util.Value;
import com.google.cloud.ReadChannel;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.osdu.client.DelfiIngestionClient;
import com.osdu.exception.IngestException;
import com.osdu.service.StorageService;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.UUID;
import javax.inject.Inject;
import org.springframework.stereotype.Service;

@Service
public class GcpStorageService implements StorageService {

  @Inject
  private Storage googleCloudStorage;
  @Inject
  private DelfiIngestionClient delfiIngestionClient;

  @Value("${gcp.storage.bucket.temp-location}")
  private String tempBucket;
  @Value("${gcp.storage.bucket.schema}")
  private String schemaBucketName;

  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public Blob uploadFileToStorage(URL fileUrl, String fileName) {

    String objectId = generateUniqueObjectId(fileName);
    Blob blobToUpload = createBlob(tempBucket, objectId);

    try (BufferedInputStream in = new BufferedInputStream(fileUrl.openStream());
        WriteChannel writer = googleCloudStorage.writer(blobToUpload)) {
      ByteStreams.copy(in, Channels.newOutputStream(writer));

      return blobToUpload;
    } catch (IOException e) {
      throw new IngestException(String.format("Error during upload file (name: %s) from URL: %s "
          + "to cloud storage", fileName, fileUrl), e);
    }
  }

  public URL writeFileToSignedUrlLocation(Blob file, URL signedUrl) {
    try (ReadChannel reader = googleCloudStorage.reader(file.getBlobId());
        WriteChannel writer = googleCloudStorage.writer(signedUrl)) {
      ByteStreams.copy(Channels.newInputStream(reader), Channels.newOutputStream(writer));

      return signedUrl;
    } catch (IOException e) {
      throw new IngestException(String.format("Error during upload file: %s from cloud storage to "
          + "signed url location: %s", file.getBlobId().getName(), signedUrl), e);
    }
  }

  @Override
  public JsonNode getSchemaByLink(String schemaLink) {
    try {
      Blob schemaBlob = createBlob(schemaBucketName, schemaLink);
      byte[] bytes = googleCloudStorage.readAllBytes(schemaBlob.getBlobId());

      return mapper.readTree(bytes);
    } catch (IOException e) {
      throw new IngestException(
          String.format("Error during read json schema: %s from cloud storage", schemaLink), e);
    }
  }

  private Blob createBlob(String bucketName, String blobName) {
    BlobId blobId = BlobId.of(bucketName, blobName);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();

    return googleCloudStorage.create(blobInfo);
  }

  private String generateUniqueObjectId(String fileName) {
    return String.format("%s/%s", UUID.randomUUID().toString(), fileName);
  }
}

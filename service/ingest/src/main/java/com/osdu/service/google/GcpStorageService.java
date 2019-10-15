package com.osdu.service.google;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.util.ByteStreams;
import com.google.cloud.ReadChannel;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.osdu.exception.IngestException;
import com.osdu.exception.OsduException;
import com.osdu.service.StorageService;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.UUID;
import javax.inject.Inject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GcpStorageService implements StorageService {

  @Inject
  Storage googleCloudStorage;

  @Inject
  CustomMediaHttpUploader uploader;

  @Value("${gcp.storage.bucket.temp-location}")
  String tempBucket;

  @Override
  public Blob uploadFileToStorage(URL fileUrl, String fileName) {

    String objectId = generateUniqueObjectId(fileName);
    Blob blobToUpload = createBlob(tempBucket, objectId);

    try (BufferedInputStream in = new BufferedInputStream(fileUrl.openStream());
        WriteChannel writer = googleCloudStorage.writer(blobToUpload)) {
      ByteStreams.copy(in, Channels.newOutputStream(writer));

    } catch (IOException e) {
      throw new IngestException(String.format("Error during upload file (name: %s) from URL: %s "
          + "to cloud storage", fileName, fileUrl), e);
    }

    return googleCloudStorage.get(tempBucket, objectId);
  }

  @Override
  public HttpResponse writeFileToSignedUrlLocation(Blob blob, URL signedUrl) {
    try (ReadChannel reader = googleCloudStorage.reader(blob.getBlobId())) {
      InputStreamContent mediaContent = new InputStreamContent("text/plain",
          new BufferedInputStream(Channels.newInputStream(reader)));
      mediaContent.setLength(blob.getSize());

      HttpResponse response = uploader.resumableUpload(mediaContent, new GenericUrl(signedUrl));
      if (!response.isSuccessStatusCode()) {
        throw new OsduException("Not success status of signed url file upload request. "
            + "Status: " + response.getStatusCode() + "\n message: " + response.getStatusMessage());
      }

      return response;
    } catch (IOException e) {
      throw new IngestException(String.format("Error during upload file: %s from cloud storage to "
          + "signed url location: %s", blob.getBlobId().getName(), signedUrl), e);
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

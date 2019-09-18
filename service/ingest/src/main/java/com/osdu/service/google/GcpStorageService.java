package com.osdu.service.google;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.osdu.service.StorageService;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

public class GcpStorageService implements StorageService {


  @Inject
  private Storage googleCloudStorage;

  public void writeFileToStorage(URL signedUrl) {

    try (WriteChannel writer = googleCloudStorage.writer(signedUrl)) {
      byte[] bytes = "hello".getBytes();
      writer.write(ByteBuffer.wrap(bytes, 0, bytes.length));
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public URL getSignedUrl(){
    BlobInfo blobinfo = BlobInfo.newBuilder(BlobId.of("pavel-test-bucket", "empty.txt")).build();

// Generate Signed URL
    URL url =
        googleCloudStorage.signUrl(blobinfo, 15, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature());

    System.out.println("Generated GET signed URL:");
    System.out.println(url);
    System.out.println("You can use this URL with any user agent, for example:");
    System.out.println("curl '" + url + "'");

    return url;
  }
}

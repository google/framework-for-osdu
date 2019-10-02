package com.osdu.service;

import com.google.api.client.http.HttpResponse;
import com.google.cloud.storage.Blob;
import java.net.URL;

public interface StorageService {

  Blob uploadFileToStorage(URL fileUrl, String fileName);

  HttpResponse writeFileToSignedUrlLocation(Blob file, URL signedUrl);
}

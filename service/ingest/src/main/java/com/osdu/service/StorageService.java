package com.osdu.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.cloud.storage.Blob;
import java.net.URL;

public interface StorageService {

  JsonNode getSchemaByLink(String schemaLink);

  Blob uploadFileToStorage(URL fileUrl, String fileName);

  URL writeFileToSignedUrlLocation(Blob file, URL signedUrl);
}

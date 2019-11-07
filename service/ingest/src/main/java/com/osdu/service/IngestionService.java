package com.osdu.service;

import com.osdu.model.Record;
import com.osdu.model.delfi.IngestedFile;
import com.osdu.model.delfi.RequestMeta;
import com.osdu.model.delfi.signed.SignedFile;
import com.osdu.model.delfi.signed.SignedUrlResult;
import com.osdu.model.manifest.ManifestFile;
import com.osdu.model.manifest.WorkProductComponent;
import java.net.URL;
import java.util.List;

public interface IngestionService {

  String createRecordForWorkProductComponent(WorkProductComponent workProductComponent,
      List<String> srns, RequestMeta requestMeta);

  SignedFile uploadFile(ManifestFile file, String authorizationToken, String partition);

  SignedUrlResult transferFile(URL fileUrl, String authToken, String partition);

  List<Record> failSubmittedFiles(List<IngestedFile> ingestedFiles,
      RequestMeta requestMeta);

  List<Record> failRecords(List<Record> records, RequestMeta requestMeta);
}

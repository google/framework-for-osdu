package com.osdu.model.delfi.signed;

import com.osdu.model.manifest.ManifestFile;
import java.net.URL;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignedFile {

  ManifestFile file;
  URL locationUrl;
  String relativeFilePath;

}

package com.osdu.model.upload;

import com.google.api.client.http.AbstractInputStreamContent;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ContentChunk {

  final AbstractInputStreamContent content;
  final String contentRange;
  final Integer length;

}

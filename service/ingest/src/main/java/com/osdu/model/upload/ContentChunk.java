package com.osdu.model.upload;

import com.google.api.client.http.AbstractInputStreamContent;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ContentChunk {

  private final AbstractInputStreamContent content;
  private final String contentRange;
  private final Integer length;

}

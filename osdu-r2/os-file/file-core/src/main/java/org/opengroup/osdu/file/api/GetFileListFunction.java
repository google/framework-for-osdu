/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.file.api;

import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.FileListResponse;
import org.opengroup.osdu.file.provider.interfaces.FileListService;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GetFileListFunction
    implements Function<Message<FileListRequest>, Message<FileListResponse>> {

  final FileListService fileListService;

  @Override
  public Message<FileListResponse> apply(Message<FileListRequest> message) {
    log.debug("File list request received, with following parameters: {}", message);
    FileListResponse fileListResponse = fileListService
        .getFileList(message.getPayload(), message.getHeaders());
    log.debug("File list result ready : {}", fileListResponse);
    return new GenericMessage<>(fileListResponse);
  }
}

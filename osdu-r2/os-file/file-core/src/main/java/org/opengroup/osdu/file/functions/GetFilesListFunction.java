/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.file.functions;

import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.model.file.FilesListRequest;
import org.opengroup.osdu.core.common.model.file.FilesListResponse;
import org.opengroup.osdu.file.service.FilesListService;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetFilesListFunction
    implements Function<Message<FilesListRequest>, Message<FilesListResponse>> {

  final FilesListService filesListService;

  @Override
  public Message<FilesListResponse> apply(Message<FilesListRequest> message) {
    return new GenericMessage<>(
        filesListService.getFilesList(message.getPayload(), message.getHeaders()));
  }
}

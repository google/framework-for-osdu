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

package org.opengroup.osdu.file.functions;

import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.model.file.FileRequest;
import org.opengroup.osdu.core.common.model.file.FileResponse;
import org.opengroup.osdu.file.provider.interfaces.FileService;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetFileFunction implements Function<Message<FileRequest>, Message<FileResponse>> {

  final FileService fileService;

  @Override
  public Message<FileResponse> apply(Message<FileRequest> message) {
    return new GenericMessage<>(fileService.getFile(message.getPayload(), message.getHeaders()));
  }
}

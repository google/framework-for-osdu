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

package org.opengroup.osdu.delivery.api;

import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.file.FileLocationRequest;
import org.opengroup.osdu.core.common.model.file.FileLocationResponse;
import org.opengroup.osdu.delivery.provider.interfaces.LocationService;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GetFileLocationFunction
    implements Function<Message<FileLocationRequest>, Message<FileLocationResponse>> {

  final LocationService locationService;

  @Override
  public Message<FileLocationResponse> apply(Message<FileLocationRequest> message) {
    log.debug("File location request received, with following parameters: {}", message);
    FileLocationResponse fileLocationResponse = locationService
        .getFileLocation(message.getPayload(), message.getHeaders());
    log.debug("File location result ready : {}", fileLocationResponse);
    return new GenericMessage<>(fileLocationResponse);
  }
}

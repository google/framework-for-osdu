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

package org.opengroup.osdu.delivery.service;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.opengroup.osdu.delivery.TestUtils.AUTHORIZATION_TOKEN;
import static org.opengroup.osdu.delivery.TestUtils.PARTITION;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.file.FileRequest;
import org.opengroup.osdu.delivery.model.Headers;
import org.opengroup.osdu.delivery.provider.interfaces.FileService;
import org.springframework.messaging.MessageHeaders;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

  private FileService fileService;

  @BeforeEach
  void setUp() {
    fileService = new FileServiceImpl();
  }

  @Test
  void shouldThrownUnsupportedException() {
    // given
    FileRequest request = FileRequest.builder()
        .build();
    MessageHeaders headers = getMessageHeaders();

    // when
    Throwable thrown = catchThrowable(() -> fileService.getFile(request, headers));

    // then
    then(thrown).isInstanceOf(UnsupportedOperationException.class);
  }

  private MessageHeaders getMessageHeaders() {
    Map<String, Object> headers = new HashMap<>();
    headers.put(Headers.AUTHORIZATION, AUTHORIZATION_TOKEN);
    headers.put(Headers.PARTITION, PARTITION);

    return new MessageHeaders(headers);
  }

}

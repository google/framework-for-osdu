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
import org.opengroup.osdu.core.common.model.file.LocationRequest;
import org.opengroup.osdu.core.common.model.file.LocationResponse;
import org.opengroup.osdu.delivery.provider.interfaces.LocationService;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GetLocationFunction
    implements Function<Message<LocationRequest>, Message<LocationResponse>> {

  final LocationService locationService;

  @Override
  public Message<LocationResponse> apply(Message<LocationRequest> message) {
    log.debug("Location request received, with following parameters: {}", message);
    LocationResponse locationResponse = locationService
        .getLocation(message.getPayload(), message.getHeaders());
    log.debug("Location result ready : {}", locationResponse);
    return new GenericMessage<>(locationResponse);
  }

}

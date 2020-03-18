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

package org.opengroup.osdu.delivery.mapper;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.opengroup.osdu.delivery.model.Headers;
import org.springframework.messaging.MessageHeaders;

@Mapper
@DecoratedWith(HeadersMapperDecorator.class)
public interface HeadersMapper {

  /**
   * Transforms message headers from the received request to needful format.
   *
   * @param headers MessageHeaders object from the request
   * @return headers that will be accepted by system
   */
  Headers toHeaders(MessageHeaders headers);

}
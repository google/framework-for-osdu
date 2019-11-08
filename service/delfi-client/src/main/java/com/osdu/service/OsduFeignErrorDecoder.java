/*
 * Copyright 2019 Google LLC
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

package com.osdu.service;

import static feign.FeignException.errorStatus;

import com.osdu.exception.OsduBadRequestException;
import com.osdu.exception.OsduException;
import com.osdu.exception.OsduForbiddenException;
import com.osdu.exception.OsduNotFoundException;
import com.osdu.exception.OsduServerErrorException;
import com.osdu.exception.OsduUnauthorizedException;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OsduFeignErrorDecoder implements ErrorDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {
    FeignException feignException = errorStatus(methodKey, response);

    switch (response.status()) {
      case 400:
        return new OsduBadRequestException("Bad request", feignException);
      case 401:
        return new OsduUnauthorizedException("Unauthorized", feignException);
      case 403:
        return new OsduForbiddenException("Forbidden", feignException);
      case 404:
        return new OsduNotFoundException("Not found", feignException);
      case 500:
        return new OsduServerErrorException("Internal server error", feignException);
      default:
        return new OsduException("Unknown feignException", feignException);
    }
  }
}

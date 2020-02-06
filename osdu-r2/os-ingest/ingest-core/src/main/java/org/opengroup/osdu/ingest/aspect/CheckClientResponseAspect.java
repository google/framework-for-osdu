/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.ingest.aspect;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.opengroup.osdu.core.common.exception.OsduBadRequestException;
import org.opengroup.osdu.core.common.exception.OsduException;
import org.opengroup.osdu.core.common.exception.OsduNotFoundException;
import org.opengroup.osdu.core.common.exception.OsduUnauthorizedException;
import org.opengroup.osdu.ingest.exception.OsduForbiddenException;
import org.opengroup.osdu.ingest.exception.OsduRuntimeException;
import org.opengroup.osdu.ingest.exception.OsduServerErrorException;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Aspect
@Configuration
@Slf4j
public class CheckClientResponseAspect {

  @AfterReturning(value = "@annotation(org.opengroup.osdu.ingest.aspect.CheckClientResponse)", returning = "response")
  public void checkResponse(feign.Response response) {
    try {
      HttpStatus httpStatus = HttpStatus.valueOf(response.status());
      if (httpStatus.isError()) {

        String errorMessage = IOUtils.toString(response.body().asInputStream(), "utf-8");
        log.error("Client exception status - {}, message - {}", httpStatus, errorMessage);

        switch (httpStatus.value()) {
          case 400:
            throw new OsduBadRequestException(errorMessage);
          case 401:
            throw new OsduUnauthorizedException(errorMessage);
          case 403:
            throw new OsduForbiddenException(errorMessage);
          case 404:
            throw new OsduNotFoundException(errorMessage);
          case 500:
            throw new OsduServerErrorException(errorMessage);
          default:
            throw new OsduException(errorMessage);
        }
      }
    } catch (IOException exception) {
      throw new OsduRuntimeException("Exception in check client response aspect", exception);
    }
  }
}

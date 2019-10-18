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

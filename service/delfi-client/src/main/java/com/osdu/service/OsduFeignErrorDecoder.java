package com.osdu.service;

import com.osdu.exception.OsduBadRequestException;
import com.osdu.exception.OsduException;
import com.osdu.exception.OsduForbiddenException;
import com.osdu.exception.OsduNotFoundException;
import com.osdu.exception.OsduServerErrorException;
import com.osdu.exception.OsduUnauthorizedException;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OsduFeignErrorDecoder implements ErrorDecoder {

  private static Map<Integer, OsduException> exceptionToStatusMap = new HashMap<>();

  /**
   * Constructor for OsduFeignErrorDecoder.
   */
  public OsduFeignErrorDecoder() {
    exceptionToStatusMap.put(400, new OsduBadRequestException("Bad request"));
    exceptionToStatusMap.put(401, new OsduUnauthorizedException("Unauthorized"));
    exceptionToStatusMap.put(403, new OsduForbiddenException("Forbidden"));
    exceptionToStatusMap.put(404, new OsduNotFoundException("Not found"));
    exceptionToStatusMap.put(500, new OsduServerErrorException("Internal server error"));
  }

  @Override
  public Exception decode(String methodKey, Response response) {

    return exceptionToStatusMap
        .getOrDefault(response.status(), new OsduException("Unknown exception"));
  }
}

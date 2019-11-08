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

package com.osdu.async;

import java.lang.reflect.Method;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

@Slf4j
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

  @Override
  public void handleUncaughtException(Throwable ex, Method method, Object... objects) {
    String msg = "Async Exception**************************************************************"
        + "\nmethod happen: " + method
        + "\nmethod params: " + Arrays.toString(objects)
        + "\nException class: {}" + ex.getClass().getName()
        + "\nex.getMessage(): {}" + ex.getMessage()
        + "\n**************************************************************";
    log.error(msg, ex);
  }
}

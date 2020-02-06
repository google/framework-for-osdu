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

package org.opengroup.osdu.workflow;

import java.lang.reflect.Method;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayNameGenerator;

/**
 * JUnit display name generator. It replaces a "camelCase" name
 * by "Capitalized Whitespace Separated" name.
 */
public class ReplaceCamelCase extends DisplayNameGenerator.Standard {

  @Override
  public String generateDisplayNameForClass(Class<?> testClass) {
    return replaceCapitals(super.generateDisplayNameForClass(testClass));
  }

  @Override
  public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
    return replaceCapitals(super.generateDisplayNameForNestedClass(nestedClass));
  }

  @Override
  public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
    String name = replaceCapitals(testMethod.getName());
    return testMethod.getParameterCount() > 0
        ? name + DisplayNameGenerator.parameterTypesAsString(testMethod)
        : name;

  }

  private String replaceCapitals(String name) {
    return StringUtils.capitalize(name.replaceAll("([A-Z])", " $1")
        .replaceAll("([0-9].)", " $1"));
  }

}

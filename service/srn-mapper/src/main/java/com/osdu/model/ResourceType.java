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

package com.osdu.model;

public enum ResourceType {
  WP_WELL_LOG("work-product/WellLog"),
  WPC_WELL_LOG("work-product-component/WellLog"),

  WP_DOCUMENT("work-product/Document"),
  WPC_DOCUMENT("work-product-component/Document"),

  WP_WELLBORE_MARKER("work-product/WellboreMarker"),
  WPC_WELLBORE_MARKER("work-product-component/WellboreMarker"),

  WP_WELLBORE_PATH("work-product/WellborePath"),
  WPC_WELLBORE_PATH("work-product-component/WellborePath"),

  WP_WELLBORE_TRAJECTORY("work-product/WellboreTrajectory"),
  WPC_WELLBORE_TRAJECTORY("work-product-component/WellboreTrajectory"),

  FILE_LAS2("file/las2"),
  FILE_CSV("file/csv"),
  FILE_PDF("file/pdf"),
  FILE_PATH("file/path");

  /**
   * Returns the enum constant with the specified name. The name must match
   * exactly an identifier used to declare an enum constant.
   *
   * @param type the name of the constant to return
   * @return the enum constant with the specified name
   * @throws IllegalArgumentException if the enum has no constant with the
   *         specified name
   */
  public static ResourceType fromType(String type) {
    for (ResourceType resourceType: ResourceType.values()) {
      if (resourceType.name.equals(type)) {
        return resourceType;
      }
    }

    throw new IllegalArgumentException("No constant with name " + type + " found");
  }

  private final String name;

  ResourceType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

}

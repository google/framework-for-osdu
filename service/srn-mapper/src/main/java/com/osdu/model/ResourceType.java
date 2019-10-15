package com.osdu.model;

public enum ResourceType {
  WP_WELL_LOG("work-product/WellLog"),
  WPC_WELL_LOG("work-product-component/WellLog"),

  WP_DOCUMENT("work-product/Document"),
  WPC_DOCUMENT("work-product-component/Document"),

  WP_WELLBORE_MARKER("work-product/WellboreMarker"),
  WPC_WELLBORE_MARKER("work-product-component/WellboreMarker"),

  WP_WELLBORE_PATH("work-product/WellborePath"),
  WPC_WELLBORE_PATH("work-product-component/WellborePath");

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

  private String name;

  ResourceType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

}

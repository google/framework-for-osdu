package com.osdu.config;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class GcsTestConfiguration extends GcsConfig {

  @Bean
  @Override
  public Storage googleCloudStorage() {
    return LocalStorageHelper.getOptions().getService();
  }
}
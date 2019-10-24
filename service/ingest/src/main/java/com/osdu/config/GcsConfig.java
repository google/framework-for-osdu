package com.osdu.config;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GcsConfig {

  /**
   * Bean of google cloud storage.
   */
  @Bean
  public Storage googleCloudStorage() {
    // Assuming that Application Default Credentials have been set
    // according to https://cloud.google.com/docs/authentication/production
    return StorageOptions.getDefaultInstance().getService();
  }

}

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

package org.opengroup.osdu.workflow.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.hibernate.validator.spi.cfg.ConstraintMappingContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class ValidationConfiguration {

  private final List<ConstraintMappingContributor> contributors;
  public ValidationConfiguration(Optional<List<ConstraintMappingContributor>> contributors) {
    this.contributors = contributors.orElseGet(ArrayList::new);
  }

  @Bean
  public LocalValidatorFactoryBean validatorFactory() {
    return new ValidatorFactoryBean(this.contributors);
  }

  public static class ValidatorFactoryBean extends LocalValidatorFactoryBean {

    private final List<ConstraintMappingContributor> contributors;

    ValidatorFactoryBean(List<ConstraintMappingContributor> contributors) {
      this.contributors = contributors;
    }

    @Override
    protected void postProcessConfiguration(javax.validation.Configuration<?> cfg) {
      if (cfg instanceof HibernateValidatorConfiguration) {
        HibernateValidatorConfiguration configuration = (HibernateValidatorConfiguration) cfg;
        this.contributors.forEach(contributor -> contributor.createConstraintMappings(() -> {
          DefaultConstraintMapping mapping = new DefaultConstraintMapping();
          configuration.addMapping(mapping);
          return mapping;
        }));
      }
    }
  }

}

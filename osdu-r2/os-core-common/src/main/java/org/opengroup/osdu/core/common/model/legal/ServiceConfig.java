/*
 * Copyright 2020 Google LLC
 * Copyright 2017-2019, Schlumberger
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

package org.opengroup.osdu.core.common.model.legal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;

import java.util.TimeZone;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ServiceConfig {
    // API Roles & Scopes
    public static final String LEGAL_ADMIN = "service.legal.admin";
    public static final String LEGAL_EDITOR = "service.legal.editor";
    public static final String LEGAL_USER = "service.legal.user";
    public static final String LEGAL_CRON = "cron.job";

    // Spring security adds prefix to roles
    public static final String PREFIX = "ROLE_";
    public static final String ROLE_LEGAL_ADMIN = PREFIX + LEGAL_ADMIN;
    public static final String ROLE_LEGAL_EDITOR = PREFIX + LEGAL_EDITOR;
    public static final String ROLE_LEGAL_USER = PREFIX + LEGAL_USER;
    public static final String ROLE_LEGAL_CRON = PREFIX + LEGAL_CRON;

    @Value("${AUTHORIZE_API:null}")
    public String AUTHORIZE_API;

    @Value("${LEGAL_HOSTNAME:null}")
    public String LEGAL_HOSTNAME;

    @Value("${CRON_JOB_IP:null}")
    private String cronIp;

    @Value("${REGION:null}")
    public String REGION;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    public String getAuthorizeAPI() {
        return AUTHORIZE_API;
    }

    public String getHostname() {
        return LEGAL_HOSTNAME;
    }

    public String getRegion() {
        String regionStart = REGION.substring(0, 2);
        if (regionStart.equals("us")) {
            return "us";
        } else if (regionStart.equals("eu")) {
            return "eu";
        } else {
            return "asia";
        }
    }

    public boolean isLocalEnvironment() {
        return "local".equalsIgnoreCase(activeProfile);
    }

    // public boolean isDevEnvironment() {
    //     return "dev".equalsIgnoreCase(activeProfile);
    // }

    // public boolean isTestEnvironment() {
    //     return "test".equalsIgnoreCase(activeProfile);
    // }

    // public boolean isPreProd() {
    //     return isLocalEnvironment() ||
    //             "dev".equalsIgnoreCase(activeProfile) ||
    //             "test".equalsIgnoreCase(activeProfile);
    // }

    // public boolean isProdEnvironment() {
    //     return "prod".equalsIgnoreCase(activeProfile);
    // }

    public String getCronIpAddress() {
        return cronIp;
    }

    private static ServiceConfig instance = new ServiceConfig();

    public static ServiceConfig Instance() {
        return instance;
    }

	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomization() {
    	return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder.timeZone(TimeZone.getDefault());
	}
}

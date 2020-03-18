/*
 * Copyright 2020 Google LLC
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

package org.opengroup.osdu.core.common.search;

import com.google.common.base.Strings;
import org.opengroup.osdu.core.common.model.search.DeploymentEnvironment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.PatternSyntaxException;

@Component
public class Config {

    @Value("${DEPLOYMENT_ENVIRONMENT}")
    private static String DEPLOYMENT_ENVIRONMENT;

    @Value("${ENVIRONMENT}")
    private static String ENVIRONMENT;

    @Value("${INDEXER_HOST}")
    private static String INDEXER_HOST;

    @Value("${SEARCH_HOST}")
    private static String SEARCH_HOST;

    @Value("${STORAGE_QUERY_RECORD_FOR_CONVERSION_HOST}")
    private static String STORAGE_QUERY_RECORD_FOR_CONVERSION_HOST;

    @Value("${STORAGE_QUERY_RECORD_HOST}")
    private static String STORAGE_QUERY_RECORD_HOST;

    @Value("${STORAGE_RECORDS_BATCH_SIZE}")
    private static String STORAGE_RECORDS_BATCH_SIZE;

    @Value("${STORAGE_SCHEMA_HOST}")
    private static String STORAGE_SCHEMA_HOST;

    @Value("${ENTITLEMENTS_HOST}")
    private static String ENTITLEMENTS_HOST;

    @Value("${ENTITLEMENT_TARGET_AUDIENCE}")
    private static String ENTITLEMENT_TARGET_AUDIENCE;

    @Value("${INDEXER_QUEUE_HOST}")
    private static String INDEXER_QUEUE_HOST;

    @Value("${ELASTIC_DATASTORE_KIND}")
    private static String ELASTIC_DATASTORE_KIND;

    @Value("${ELASTIC_DATASTORE_ID}")
    private static String ELASTIC_DATASTORE_ID;

    @Value("${REDIS_SEARCH_HOST}")
    private static String REDIS_SEARCH_HOST;

    @Value("${REDIS_SEARCH_PORT}")
    private static String REDIS_SEARCH_PORT;

    @Value("${SCHEMA_CACHE_EXPIRATION}")
    private static String SCHEMA_CACHE_EXPIRATION;

    @Value("${INDEX_CACHE_EXPIRATION}")
    private static String INDEX_CACHE_EXPIRATION;

    @Value("${ELASTIC_CACHE_EXPIRATION}")
    private static String ELASTIC_CACHE_EXPIRATION;

    @Value("${CURSOR_CACHE_EXPIRATION}")
    private static String CURSOR_CACHE_EXPIRATION;

    @Value("${GOOGLE_CLOUD_PROJECT}")
    private static String GOOGLE_CLOUD_PROJECT;

    @Value("${GAE_SERVICE}")
    private static String GAE_SERVICE;

    @Value("${GAE_VERSION}")
    private static String GAE_VERSION;

    @Value("${ELASTIC_HOST}")
    private static String ELASTIC_HOST;

    @Value("${ELASTIC_CLUSTER_NAME}")
    private static String ELASTIC_CLUSTER_NAME;

    @Value("${KEY_RING}")
    private static String KEY_RING;

    @Value("${KMS_KEY}")
    private static String KMS_KEY;

    @Value("${GOOGLE_AUDIENCES}")
    private static String GOOGLE_AUDIENCES;

    @Value("${CRON_INDEX_CLEANUP_PATTERN}")
    private static String CRON_INDEX_CLEANUP_PATTERN;

    @Value("${CRON_INDEX_CLEANUP_TENANTS}")
    private static String CRON_INDEX_CLEANUP_TENANTS;

    @Value("${CRON_INDEX_CLEANUP_THRESHOLD_DAYS}")
    private static String CRON_INDEX_CLEANUP_THRESHOLD_DAYS;

    @Value("${CRON_EMPTY_INDEX_CLEANUP_THRESHOLD_DAYS}")
    private static String CRON_EMPTY_INDEX_CLEANUP_THRESHOLD_DAYS;

    @Value("${SMART_SEARCH_CCS_DISABLED}")
    private static String SMART_SEARCH_CCS_DISABLED;


    public static DeploymentEnvironment getDeploymentEnvironment() {
        return Strings.isNullOrEmpty(DEPLOYMENT_ENVIRONMENT) ? DeploymentEnvironment.CLOUD : DeploymentEnvironment.valueOf(DEPLOYMENT_ENVIRONMENT);
    }

    public static String getIndexerHostUrl() {
        return !Strings.isNullOrEmpty(INDEXER_HOST) ? INDEXER_HOST : getEnvironmentVariable("INDEXER_HOST");
    }

    public static String getSearchHostUrl() {
        return !Strings.isNullOrEmpty(SEARCH_HOST) ? SEARCH_HOST : getEnvironmentVariable("SEARCH_HOST");
    }

    public static String getStorageQueryRecordFoRConversionHostUrl() {
        return !Strings.isNullOrEmpty(STORAGE_QUERY_RECORD_FOR_CONVERSION_HOST) ? STORAGE_QUERY_RECORD_FOR_CONVERSION_HOST : getEnvironmentVariable("STORAGE_QUERY_RECORD_FOR_CONVERSION_HOST");
    }

    public static String getStorageQueryRecordHostUrl() {
        return !Strings.isNullOrEmpty(STORAGE_QUERY_RECORD_HOST) ? STORAGE_QUERY_RECORD_HOST : getEnvironmentVariable("STORAGE_QUERY_RECORD_HOST");
    }

    // reduced to reflect limitation on storage record:batch api
    public static int getStorageRecordsBatchSize() {
        String storageRecordsBatchSize = !Strings.isNullOrEmpty(STORAGE_RECORDS_BATCH_SIZE) ? STORAGE_RECORDS_BATCH_SIZE : getEnvironmentVariable("STORAGE_RECORDS_BATCH_SIZE");
        return Integer.parseInt(getDefaultOrEnvironmentValue(storageRecordsBatchSize, 20));
    }

    public static String getStorageSchemaHostUrl() {
        return !Strings.isNullOrEmpty(STORAGE_SCHEMA_HOST) ? STORAGE_SCHEMA_HOST : getEnvironmentVariable("STORAGE_SCHEMA_HOST");
    }

    public static String getEntitlementsHostUrl() {
        return !Strings.isNullOrEmpty(ENTITLEMENTS_HOST) ? ENTITLEMENTS_HOST : getEnvironmentVariable("ENTITLEMENTS_HOST");
    }

    public static String getEntitlementTargetAudience() {
        return !Strings.isNullOrEmpty(ENTITLEMENT_TARGET_AUDIENCE) ? ENTITLEMENT_TARGET_AUDIENCE : getEnvironmentVariable("ENTITLEMENT_TARGET_AUDIENCE");
    }

    public static String getIndexerQueueHost() {
        return !Strings.isNullOrEmpty(INDEXER_QUEUE_HOST) ? INDEXER_QUEUE_HOST : getEnvironmentVariable("INDEXER_QUEUE_HOST");
    }

    public static String getElasticCredentialsDatastoreKind() {
        return !Strings.isNullOrEmpty(ELASTIC_DATASTORE_KIND) ? ELASTIC_DATASTORE_KIND : getEnvironmentVariable("ELASTIC_DATASTORE_KIND");
    }

    public static String getElasticCredentialsDatastoreId() {
        return !Strings.isNullOrEmpty(ELASTIC_DATASTORE_ID) ? ELASTIC_DATASTORE_ID : getEnvironmentVariable("ELASTIC_DATASTORE_ID");
    }

    public static boolean isLocalEnvironment() {
        String environment = !Strings.isNullOrEmpty(ENVIRONMENT) ? ENVIRONMENT : getEnvironmentVariable("ENVIRONMENT");
        return "local".equalsIgnoreCase(environment);
    }

    public static boolean isPreP4d() {
        String environment = !Strings.isNullOrEmpty(ENVIRONMENT) ? ENVIRONMENT : getEnvironmentVariable("ENVIRONMENT");
        return isLocalEnvironment() ||
                "evd".equalsIgnoreCase(environment) ||
                "evt".equalsIgnoreCase(environment);
    }

    public static boolean isPreDemo() {
        String environment = !Strings.isNullOrEmpty(ENVIRONMENT) ? ENVIRONMENT : getEnvironmentVariable("ENVIRONMENT");
        return isPreP4d() ||
                "p4d".equalsIgnoreCase(environment);
    }

    public static String getSearchRedisHost() {
        return !Strings.isNullOrEmpty(REDIS_SEARCH_HOST) ? REDIS_SEARCH_HOST : getEnvironmentVariable("REDIS_SEARCH_HOST");
    }

    public static int getSearchRedisPort() {
        String redisSearchPort = !Strings.isNullOrEmpty(REDIS_SEARCH_PORT) ? REDIS_SEARCH_PORT : getEnvironmentVariable("REDIS_SEARCH_PORT");
        return Integer.parseInt(getDefaultOrEnvironmentValue(redisSearchPort, 6379));
    }

    public static int getSchemaCacheExpiration() {
        String schemaCacheExpiration = !Strings.isNullOrEmpty(SCHEMA_CACHE_EXPIRATION) ? SCHEMA_CACHE_EXPIRATION : getEnvironmentVariable("SCHEMA_CACHE_EXPIRATION");
        return Integer.parseInt(getDefaultOrEnvironmentValue(schemaCacheExpiration, 60));
    }

    public static int getIndexCacheExpiration() {
        String indexCacheExpiration = !Strings.isNullOrEmpty(INDEX_CACHE_EXPIRATION) ? INDEX_CACHE_EXPIRATION : getEnvironmentVariable("INDEX_CACHE_EXPIRATION");
        return Integer.parseInt(getDefaultOrEnvironmentValue(indexCacheExpiration, 60));
    }

    public static int getKindsCacheExpiration() {
        return 2 * 24 * 60;
    }

    public static int getKindsRedisDataBase() {
        return 1;
    }

    public static int getAttributesCacheExpiration() {
        return 2 * 24 * 60;
    }

    public static int getElasticCacheExpiration() {
        String elasticCacheExpiration = !Strings.isNullOrEmpty(ELASTIC_CACHE_EXPIRATION) ? ELASTIC_CACHE_EXPIRATION : getEnvironmentVariable("ELASTIC_CACHE_EXPIRATION");
        return Integer.parseInt(getDefaultOrEnvironmentValue(elasticCacheExpiration, 1440));
    }

    public static int getCursorCacheExpiration() {
        String cursorCacheExpiration = !Strings.isNullOrEmpty(CURSOR_CACHE_EXPIRATION) ? CURSOR_CACHE_EXPIRATION : getEnvironmentVariable("CURSOR_CACHE_EXPIRATION");
        return Integer.parseInt(getDefaultOrEnvironmentValue(cursorCacheExpiration, 60));
    }


    // google cloud project
    public static String getGoogleCloudProjectId() {
        return !Strings.isNullOrEmpty(GOOGLE_CLOUD_PROJECT) ? GOOGLE_CLOUD_PROJECT : getEnvironmentVariable("GOOGLE_CLOUD_PROJECT");
    }

    public static String getDeployedServiceId() {
        return !Strings.isNullOrEmpty(GAE_SERVICE) ? GAE_SERVICE : getEnvironmentVariable("GAE_SERVICE");
    }

    public static String getDeployedVersionId() {
        return !Strings.isNullOrEmpty(GAE_VERSION) ? GAE_VERSION : getEnvironmentVariable("GAE_VERSION");
    }

    // elastic cluster settings
    public static String getElasticServerAddress() {
        return !Strings.isNullOrEmpty(ELASTIC_HOST) ? ELASTIC_HOST : getEnvironmentVariable("ELASTIC_HOST");
    }

    public static String getElasticClusterName() {
        return !Strings.isNullOrEmpty(ELASTIC_CLUSTER_NAME) ? ELASTIC_CLUSTER_NAME : getEnvironmentVariable("ELASTIC_CLUSTER_NAME");
    }


    // google KMS settings
    public static String getKmsRing() {
        String keyRing = !Strings.isNullOrEmpty(KEY_RING) ? KEY_RING : getEnvironmentVariable("KEY_RING");
        return getDefaultOrEnvironmentValue(keyRing, "csqp");
    }

    public static String getKmsKey() {
        String kmsKey = !Strings.isNullOrEmpty(KMS_KEY) ? KMS_KEY : getEnvironmentVariable("KMS_KEY");
        return getDefaultOrEnvironmentValue(kmsKey, "searchService");
    }


    //  google endpoints
    public static String getGoogleAudiences() {
        return !Strings.isNullOrEmpty(GOOGLE_AUDIENCES) ? GOOGLE_AUDIENCES : getEnvironmentVariable("GOOGLE_AUDIENCES");
    }

    public static String[] getIndexCleanupPattern() {
        String patternStr = !Strings.isNullOrEmpty(CRON_INDEX_CLEANUP_PATTERN) ? CRON_INDEX_CLEANUP_PATTERN : getEnvironmentVariable("CRON_INDEX_CLEANUP_PATTERN");
        if (!Strings.isNullOrEmpty(patternStr)) {
            try {
                return patternStr.split(",");
            } catch (PatternSyntaxException ignored) { }
        }
        return new String[0];
    }

    public static String[] getIndexCleanupTenants() {
        String patternStr = !Strings.isNullOrEmpty(CRON_INDEX_CLEANUP_TENANTS) ? CRON_INDEX_CLEANUP_TENANTS : getEnvironmentVariable("CRON_INDEX_CLEANUP_TENANTS");
        if (!Strings.isNullOrEmpty(patternStr)) {
            try {
                return patternStr.split(",");
            } catch (PatternSyntaxException ignored) { }
        }
        return new String[0];
    }

    // number of days before the indices will be cleaned, most commonly used for tests indices
    public static int getIndexCleanupThresholdDays() {
        String cronIndexCleanupThresholdDays = !Strings.isNullOrEmpty(CRON_INDEX_CLEANUP_THRESHOLD_DAYS) ? CRON_INDEX_CLEANUP_THRESHOLD_DAYS : getEnvironmentVariable("CRON_INDEX_CLEANUP_THRESHOLD_DAYS");
        return Integer.parseInt(getDefaultOrEnvironmentValue(cronIndexCleanupThresholdDays, 3));
    }

    public static int getEmptyIndexCleanupThresholdDays() {
        String cronEmptyIndexCleanupThresholdDays = !Strings.isNullOrEmpty(CRON_EMPTY_INDEX_CLEANUP_THRESHOLD_DAYS) ? CRON_EMPTY_INDEX_CLEANUP_THRESHOLD_DAYS : getEnvironmentVariable("CRON_EMPTY_INDEX_CLEANUP_THRESHOLD_DAYS");
        return Integer.parseInt(getDefaultOrEnvironmentValue(cronEmptyIndexCleanupThresholdDays, 7));
    }

    // TODO: Remove this temporary implementation when ECE CCS is utilized
    public static final Boolean isSmartSearchCcsDisabled() {
        String smartSearchCcsDisabled = !Strings.isNullOrEmpty(SMART_SEARCH_CCS_DISABLED) ? SMART_SEARCH_CCS_DISABLED : getEnvironmentVariable("SMART_SEARCH_CCS_DISABLED");
        return Boolean.TRUE.toString().equalsIgnoreCase(smartSearchCcsDisabled);
    }

    private static <T> String getDefaultOrEnvironmentValue(T givenValue, T defaultValue) {
        if (givenValue == null || Strings.isNullOrEmpty(givenValue.toString())) {
            return defaultValue.toString();
        }
        return givenValue.toString();
    }

    private static String getEnvironmentVariable(String propertyKey) {
        return System.getProperty(propertyKey, System.getenv(propertyKey));
    }

    private static Config instance = new Config();

    public static Config Instance() {
        return instance;
    }
}

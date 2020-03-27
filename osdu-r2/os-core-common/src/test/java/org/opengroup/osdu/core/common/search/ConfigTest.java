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

package org.opengroup.osdu.core.common.search;

import org.junit.Test;
import org.opengroup.osdu.core.common.model.search.DeploymentEnvironment;

import static org.junit.Assert.*;

public class ConfigTest {

    @Test
    public void checkConfig() {
        assertNotNull(new Config());

        System.setProperty("DEPLOYMENT_ENVIRONMENT", "CLOUD");
        System.setProperty("ELASTIC_DATASTORE_KIND", "BBB");
        System.setProperty("ELASTIC_DATASTORE_ID", "CCC");
        System.setProperty("ELASTIC_HOST", "DDD");
        System.setProperty("ELASTIC_CLUSTER_NAME", "FFF");
        System.setProperty("GOOGLE_CLOUD_PROJECT", "GGG");
        System.setProperty("STORAGE_SCHEMA_HOST", "KKK");
        System.setProperty("STORAGE_QUERY_RECORD_HOST", "LLL");
        System.setProperty("STORAGE_QUERY_RECORD_FOR_CONVERSION_HOST", "For");
        System.setProperty("SEARCH_HOST", "MMM");
        System.setProperty("INDEXER_HOST", "OOO");
        System.setProperty("ENTITLEMENTS_HOST", "PPP");
        System.setProperty("ENTITLEMENT_TARGET_AUDIENCE", "RRR");
        System.setProperty("GAE_SERVICE", "SSS");
        System.setProperty("GAE_VERSION", "TTT");
        System.setProperty("KEY_RING", "UUU");
        System.setProperty("KMS_KEY", "VVV");
        System.setProperty("STORAGE_RECORDS_BATCH_SIZE", "50");
        System.setProperty("ELASTIC_CACHE_EXPIRATION", "14");
        System.setProperty("ELASTIC_CACHE_SIZE", "15");
        System.setProperty("SCHEMA_CACHE_EXPIRATION", "14");
        System.setProperty("SCHEMA_CACHE_SIZE", "15");
        System.setProperty("CURSOR_CACHE_EXPIRATION", "15");
        System.setProperty("CRON_INDEX_CLEANUP_THRESHOLD_DAYS", "1");
        System.setProperty("CRON_EMPTY_INDEX_CLEANUP_THRESHOLD_DAYS", "2");

        System.setProperty("INDEXER_QUEUE_HOST", "evd");
        System.setProperty("REDIS_SEARCH_HOST", "10.5.0.10");
        System.setProperty("REDIS_SEARCH_PORT", "6379");
        System.setProperty("INDEX_CACHE_EXPIRATION", "10");

        System.setProperty("GOOGLE_AUDIENCES", "google-aud");

        assertEquals(DeploymentEnvironment.CLOUD, Config.getDeploymentEnvironment());
        assertEquals("BBB", Config.getElasticCredentialsDatastoreKind());
        assertEquals("CCC", Config.getElasticCredentialsDatastoreId());
        assertEquals("DDD", Config.getElasticServerAddress());
        assertEquals("FFF", Config.getElasticClusterName());
        assertEquals("GGG", Config.getGoogleCloudProjectId());
        assertEquals("KKK", Config.getStorageSchemaHostUrl());
        assertEquals("LLL", Config.getStorageQueryRecordHostUrl());
        assertEquals("For", Config.getStorageQueryRecordFoRConversionHostUrl());
        assertEquals("MMM", Config.getSearchHostUrl());

        assertEquals("PPP", Config.getEntitlementsHostUrl());
        assertEquals("RRR", Config.getEntitlementTargetAudience());

        assertEquals("TTT", Config.getDeployedVersionId());
        assertEquals("UUU", Config.getKmsRing());
        assertEquals("VVV", Config.getKmsKey());
        assertEquals(50, Config.getStorageRecordsBatchSize());
        assertEquals(14, Config.getSchemaCacheExpiration());
        assertEquals(14, Config.getElasticCacheExpiration());
        assertEquals(15, Config.getCursorCacheExpiration());
        assertEquals(1, Config.getIndexCleanupThresholdDays());
        assertEquals(2, Config.getEmptyIndexCleanupThresholdDays());

        assertEquals("evd", Config.getIndexerQueueHost());
        assertEquals(10, Config.getIndexCacheExpiration());
        assertEquals(6379, Config.getSearchRedisPort());
        assertEquals("10.5.0.10", Config.getSearchRedisHost());

        assertEquals("google-aud", Config.getGoogleAudiences());

        assertEquals(2 * 24 * 60, Config.getKindsCacheExpiration());
        assertEquals(1, Config.getKindsRedisDataBase());
        assertEquals(2 * 24 * 60, Config.getAttributesCacheExpiration());
    }

    @Test
    public void try_get_invalid_pattern() {
        System.setProperty("CRON_INDEX_CLEANUP_PATTERN", "");
        String[] result = Config.getIndexCleanupPattern();
        assertEquals(result.length, 0);

        System.setProperty("CRON_INDEX_CLEANUP_PATTERN", "blah;fdjo");
        result = Config.getIndexCleanupPattern();
        assertEquals(result.length, 1);

        System.setProperty("CRON_INDEX_CLEANUP_PATTERN", "-testindex*,-testquery*,-testcursor*");
        result = Config.getIndexCleanupPattern();
        assertEquals(result.length, 3);
    }

    @Test
    public void try_get_indexCleanUp_tenants() {
        System.setProperty("CRON_INDEX_CLEANUP_TENANTS", "");
        String[] result = Config.getIndexCleanupTenants();
        assertEquals(result.length, 0);

        System.setProperty("CRON_INDEX_CLEANUP_TENANTS", "tenant1;fdjo");
        result = Config.getIndexCleanupTenants();
        assertEquals(result.length, 1);

        System.setProperty("CRON_INDEX_CLEANUP_TENANTS", "tenant1,common");
        result = Config.getIndexCleanupTenants();
        assertEquals(result.length, 2);
    }

    @Test
    public void get_env() {
        System.setProperty("ENVIRONMENT", "evd");
        assertTrue(Config.isPreDemo());
        assertTrue(Config.isPreP4d());
        assertFalse(Config.isLocalEnvironment());
    }

    // TODO: Remove this temporary implementation when ECE CCS is utilized
    @Test
    public void try_get_ccsStatus() {
        System.setProperty("SMART_SEARCH_CCS_DISABLED", "true");
        assertTrue(Config.isSmartSearchCcsDisabled());

        System.setProperty("SMART_SEARCH_CCS_DISABLED", "");
        assertFalse(Config.isSmartSearchCcsDisabled());
    }
}

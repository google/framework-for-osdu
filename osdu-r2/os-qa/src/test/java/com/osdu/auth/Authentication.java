/*
 * Copyright  2020 Google LLC
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

package com.osdu.auth;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.StorageOptions;
import com.osdu.core.reporter.TestReporter;
import org.testng.annotations.BeforeClass;

import java.io.FileInputStream;
import java.io.IOException;

import static com.osdu.utils.EnvironmentVariableReceiver.getGoogleCredentialFile;

public class Authentication {
    /**
     * Authentication
     * authExplicit(getGoogleCredentialFile());
     * for the remote run create variable GOOGLE_APPLICATION_CREDENTIALS
     * <p>
     * path to the end user credentials from Google Cloud SDK
     */
    @BeforeClass
    static void authExplicit() {
        // You can specify a credential file by providing a path to GoogleCredentials.
        // Otherwise credentials are read from the GOOGLE_APPLICATION_CREDENTIALS environment variable.
        try {
            StorageOptions.newBuilder()
                    .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(getGoogleCredentialFile())))
                    .build()
                    .getService();
            TestReporter.reportStep("Authentication was completed");
        } catch (IOException e) {
            TestReporter.reportErrorStep(e.toString());
        }
    }
}
/*
 * Copyright  2019 Google LLC
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

package com.osdu.core.utils.helper.environment;

import com.osdu.core.reporter.TestReporter;
import lombok.SneakyThrows;

import static com.osdu.core.utils.helper.environment.enums.Environments.*;

public class EnvironmentDetector {
    public static String OS;

    @SneakyThrows
    public static Enum runCmdScriptInAccordingToTheEnvironment() {
        OS = System.getProperty("os.name").toLowerCase();

        TestReporter.reportStep("Check environment");
        if (isWindows()) {
            TestReporter.reportStep("Current environment is Windows");
            return WINDOWS;
        } else if (isUnix()) {
            TestReporter.reportStep("Current environment is Linux");
            return LINUX;
        } else if (isMac()) {
            TestReporter.reportStep("Current environment is Mac");
            return MAC;
        } else {
            TestReporter.reportWarningStep("Environments is not detected");
            throw new Exception("Not detected environment");
        }
    }

    private static boolean isWindows() {
        return (OS.contains("windows"));
    }

    private static boolean isMac() {
        return (OS.contains("mac"));
    }

    private static boolean isUnix() {
        return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix") || OS.contains("linux"));
    }
}
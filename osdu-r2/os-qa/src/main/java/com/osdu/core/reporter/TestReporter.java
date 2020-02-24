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

package com.osdu.core.reporter;

import io.qameta.allure.Step;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TestReporter {
    /**
     * Report info on basic occasion during testing
     * @param stepMessage message
     * @param parameters additional params
     */
    public static void reportStep(String stepMessage, Object... parameters) {
        String message = String.format(stepMessage, parameters);

        log.info(message);
        reportAllureStep(message);
    }

    /**
     * Report all calls and method usages during code implementation
     * @param debugStepMessage message
     * @param parameters additional params
     */
    public static void reportDebugStep(String debugStepMessage, Object... parameters) {
        String message = String.format(debugStepMessage, parameters);

        log.debug(message);
        reportAllureStep(message);
    }

    /**
     * Report all suspicious calls where is probability of code failure
     * @param warningStepMessage message
     * @param parameters additional params
     */
    public static void reportWarningStep(String warningStepMessage, Object... parameters) {
        String message = String.format(warningStepMessage, parameters);

        log.warn(message);
        reportAllureStep(message);
    }

    /**
     * Report error messages
     * @param errorStepMessage message
     * @param parameters additional params
     */
    public static void reportErrorStep(String errorStepMessage, Object... parameters) {
        String message = String.format(errorStepMessage, parameters);

        log.error(message);
        reportAllureStep(message);
    }

    @Step("{0}")
    private static void reportAllureStep(String stepMessage) { }
}
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

package com.osdu.utils;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import static com.osdu.core.utils.helper.environment.EnvironmentDetector.runCmdScriptInAccordingToTheEnvironment;
import static com.osdu.core.utils.helper.environment.enums.Environments.*;

public class TerminalRunner {
    static Process process;
    public static String closeAllCmds = "taskkill /IM  cmd.exe";

    public static String runTerminalForWindow = "cmd /c start cmd.exe";
    public static String winOpenedTerminalReadyForRequest = "cmd.exe /K ";
    public static String startFireStoreFromWindows = "cmd /c start cmd.exe /K gcloud beta emulators firestore start --host-port=localhost:9090 ";
    static String fileServicePath= "src/test/resources/data/gcp-services/file.jar";

    public static String fileServiceInstanceCreator(){
        return StringUtils.join("cmd /c start cmd.exe /K ",
                "java -jar ", fileServicePath);
    }

    public static void runTerminal(String runTerminal) {
        try {
            process = Runtime.getRuntime().exec(runTerminal);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void commandExecutor(String runSpecifiedTerminal, String commandToRun) {
        try {
            process = Runtime.getRuntime().exec(runSpecifiedTerminal + StringUtils.SPACE + commandToRun);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public static void closeCmd() {
        if (WINDOWS.equals(runCmdScriptInAccordingToTheEnvironment())) {
            commandExecutor(winOpenedTerminalReadyForRequest, closeAllCmds);
        } else if (LINUX.equals(runCmdScriptInAccordingToTheEnvironment()) ||
                MAC.equals(runCmdScriptInAccordingToTheEnvironment())) {
        }
    }
}
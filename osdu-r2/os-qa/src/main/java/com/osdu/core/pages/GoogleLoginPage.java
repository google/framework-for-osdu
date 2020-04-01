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

package com.osdu.core.pages;

import com.codeborne.selenide.SelenideElement;
import com.osdu.core.reporter.TestReporter;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byId;
import static com.codeborne.selenide.Selectors.byXpath;
import static com.codeborne.selenide.Selenide.$;

public class GoogleLoginPage {
    final static SelenideElement gmailField = $(byId("identifierId"));
    final static SelenideElement nextButton = $(byId("identifierNext"));
    final static SelenideElement passwordNext = $(byId("passwordNext"));
    final static SelenideElement passwordField = $(byXpath("//*[@type='password']"));

    /**
     * Login to the mail account
     *
     * @param username string
     * @param password string
     * @return this page instance
     */
    public void doLogin(String username, String password) {
        gmailField.shouldBe(visible).sendKeys(username);
        TestReporter.reportDebugStep("Login field was filled in with thelogin");

        nextButton.waitUntil(visible, 20);
        nextButton.shouldBe(visible).click();
        TestReporter.reportDebugStep("Next button is clicked");

        passwordField.sendKeys(password);
        TestReporter.reportDebugStep("Password field was filled in with the password");

        passwordNext.waitUntil(visible, 20);
        passwordNext.shouldBe(visible).click();
        TestReporter.reportDebugStep("Next button is clicked");
    }
}

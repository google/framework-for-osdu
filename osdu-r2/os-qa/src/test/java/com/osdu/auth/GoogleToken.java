package com.osdu.auth;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import com.osdu.core.data.provider.DataProviders;
import com.osdu.core.pages.GoogleLoginPage;
import com.osdu.core.pages.GoogleProviderPage;
import io.qameta.allure.Description;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.open;
import static com.osdu.core.utils.helper.EnvironmentVariableReceiver.*;

public class GoogleToken {
    GoogleLoginPage loginPage = new GoogleLoginPage();
    GoogleProviderPage providerPage = new GoogleProviderPage();
    final String PAGE_URL = getTokenPage();

    @BeforeClass
    public void startBrowser(){
        Configuration.browser = "chrome";
        open(PAGE_URL);
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Login to the account and receive token")
    public void getToken(Map<String,String> data) {
        providerPage.getGoogleProvider().shouldBe(visible).click();
        loginPage.doLogin(getGoogleLogin(), getGooglePassword());

        //todo:
        //get token
        //save token
    }

    @AfterClass
    public void tearDown() {
        WebDriverRunner.getWebDriver().close();
    }
}
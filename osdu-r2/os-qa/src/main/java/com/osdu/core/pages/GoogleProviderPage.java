package com.osdu.core.pages;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selectors.byId;
import static com.codeborne.selenide.Selenide.$;

@Getter
public class GoogleProviderPage {
    SelenideElement googleProvider = $(byId("submit"));
}

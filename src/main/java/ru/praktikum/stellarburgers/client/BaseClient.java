package ru.praktikum.stellarburgers.client;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public abstract class BaseClient {
    private static final String BASE_URI = "https://qa-stellarburgers.education-services.ru";
    private static final String BASE_PATH = "/api";
    private static final int TIMEOUT_MILLIS = 15_000;

    protected RequestSpecification getSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URI)
                .setBasePath(BASE_PATH)
                .setContentType(ContentType.JSON)
                .setConfig(RestAssuredConfig.config().httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", TIMEOUT_MILLIS)
                        .setParam("http.socket.timeout", TIMEOUT_MILLIS)))
                .addFilter(new AllureRestAssured())
                .build();
    }
}

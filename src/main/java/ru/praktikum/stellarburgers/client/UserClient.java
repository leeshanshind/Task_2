package ru.praktikum.stellarburgers.client;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import ru.praktikum.stellarburgers.model.User;
import ru.praktikum.stellarburgers.model.UserCredentials;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class UserClient extends BaseClient {
    private static final String AUTH_PATH = "/auth";
    private static final String REGISTER_PATH = AUTH_PATH + "/register";
    private static final String LOGIN_PATH = AUTH_PATH + "/login";
    private static final String USER_PATH = AUTH_PATH + "/user";
    private static final String AUTHORIZATION = "Authorization";

    @Step("Зарегистрировать пользователя")
    public ValidatableResponse register(User user) {
        return given()
                .spec(getSpec())
                .body(user)
                .when()
                .post(REGISTER_PATH)
                .then();
    }

    @Step("Залогиниться пользователем")
    public ValidatableResponse login(UserCredentials credentials) {
        return given()
                .spec(getSpec())
                .body(credentials)
                .when()
                .post(LOGIN_PATH)
                .then();
    }

    @Step("Изменить данные пользователя с авторизацией: {fields}")
    public ValidatableResponse update(String accessToken, Map<String, String> fields) {
        return given()
                .spec(getSpec())
                .header(AUTHORIZATION, accessToken)
                .body(fields)
                .when()
                .patch(USER_PATH)
                .then();
    }

    @Step("Изменить данные пользователя без авторизации: {fields}")
    public ValidatableResponse updateWithoutAuth(Map<String, String> fields) {
        return given()
                .spec(getSpec())
                .body(fields)
                .when()
                .patch(USER_PATH)
                .then();
    }

    @Step("Удалить пользователя")
    public ValidatableResponse delete(String accessToken) {
        return given()
                .spec(getSpec())
                .header(AUTHORIZATION, accessToken)
                .when()
                .delete(USER_PATH)
                .then();
    }
}

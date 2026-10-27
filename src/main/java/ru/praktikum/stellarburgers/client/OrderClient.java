package ru.praktikum.stellarburgers.client;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import ru.praktikum.stellarburgers.model.Order;

import static io.restassured.RestAssured.given;

public class OrderClient extends BaseClient {
    private static final String INGREDIENTS_PATH = "/ingredients";
    private static final String ORDERS_PATH = "/orders";
    private static final String AUTHORIZATION = "Authorization";

    @Step("Получить список ингредиентов")
    public ValidatableResponse getIngredients() {
        return given()
                .spec(getSpec())
                .when()
                .get(INGREDIENTS_PATH)
                .then();
    }

    @Step("Создать заказ с авторизацией")
    public ValidatableResponse create(String accessToken, Order order) {
        return given()
                .spec(getSpec())
                .header(AUTHORIZATION, accessToken)
                .body(order)
                .when()
                .post(ORDERS_PATH)
                .then();
    }

    @Step("Создать заказ без авторизации")
    public ValidatableResponse createWithoutAuth(Order order) {
        return given()
                .spec(getSpec())
                .body(order)
                .when()
                .post(ORDERS_PATH)
                .then();
    }

    @Step("Получить заказы пользователя с авторизацией")
    public ValidatableResponse getUserOrders(String accessToken) {
        return given()
                .spec(getSpec())
                .header(AUTHORIZATION, accessToken)
                .when()
                .get(ORDERS_PATH)
                .then();
    }

    @Step("Получить заказы пользователя без авторизации")
    public ValidatableResponse getUserOrdersWithoutAuth() {
        return given()
                .spec(getSpec())
                .when()
                .get(ORDERS_PATH)
                .then();
    }
}

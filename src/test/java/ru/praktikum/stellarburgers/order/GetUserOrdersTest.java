package ru.praktikum.stellarburgers.order;

import io.qameta.allure.Description;
import io.qameta.allure.junit5.AllureJunit5;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.praktikum.stellarburgers.client.OrderClient;
import ru.praktikum.stellarburgers.client.UserClient;
import ru.praktikum.stellarburgers.generator.OrderGenerator;
import ru.praktikum.stellarburgers.generator.UserGenerator;
import ru.praktikum.stellarburgers.model.Order;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(AllureJunit5.class)
@DisplayName("Заказы пользователя: GET /api/orders")
class GetUserOrdersTest {
    private static final String UNAUTHORISED_MESSAGE = "You should be authorised";

    private final UserClient userClient = new UserClient();
    private final OrderClient orderClient = new OrderClient();
    private String accessToken;
    private Order burger;
    private int orderNumber;

    @BeforeEach
    void setUp() {
        accessToken = userClient.register(UserGenerator.randomUser())
                .statusCode(SC_OK)
                .extract().path("accessToken");
        // создаём заказ, чтобы у пользователя был ровно один заказ
        burger = OrderGenerator.burger(orderClient.getIngredients().statusCode(SC_OK));
        orderNumber = orderClient.create(accessToken, burger)
                .statusCode(SC_OK)
                .extract().path("order.number");
    }

    @AfterEach
    void tearDown() {
        if (accessToken != null) {
            userClient.delete(accessToken);
        }
    }

    @Test
    @DisplayName("Авторизованный пользователь получает свои заказы")
    @Description("Запрос с токеном возвращает 200 и список с созданным заказом")
    void authorizedUserGetsOwnOrders() {
        orderClient.getUserOrders(accessToken)
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("orders", hasSize(1))
                .body("orders[0].number", equalTo(orderNumber))
                .body("orders[0].ingredients", equalTo(burger.getIngredients()))
                .body("total", notNullValue())
                .body("totalToday", notNullValue());
    }

    @Test
    @DisplayName("Неавторизованный пользователь не может получить заказы")
    @Description("Запрос без токена возвращает 401")
    void unauthorizedUserCannotGetOrders() {
        orderClient.getUserOrdersWithoutAuth()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo(UNAUTHORISED_MESSAGE));
    }
}

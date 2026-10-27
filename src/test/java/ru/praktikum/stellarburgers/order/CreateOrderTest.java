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
import ru.praktikum.stellarburgers.model.User;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith(AllureJunit5.class)
@DisplayName("Создание заказа: POST /api/orders")
class CreateOrderTest {
    private static final String NO_INGREDIENTS_MESSAGE = "Ingredient ids must be provided";
    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";

    private final UserClient userClient = new UserClient();
    private final OrderClient orderClient = new OrderClient();
    private User user;
    private String accessToken;
    private Order burger;

    @BeforeEach
    void setUp() {
        user = UserGenerator.randomUser();
        accessToken = userClient.register(user)
                .statusCode(SC_OK)
                .extract().path("accessToken");
        burger = OrderGenerator.burger(orderClient.getIngredients().statusCode(SC_OK));
    }

    @AfterEach
    void tearDown() {
        if (accessToken != null) {
            userClient.delete(accessToken);
        }
    }

    @Test
    @DisplayName("Авторизованный пользователь может создать заказ с ингредиентами")
    @Description("Запрос с токеном возвращает 200, номер заказа, его ингредиенты и владельца")
    void authorizedUserCanCreateOrderWithIngredients() {
        orderClient.create(accessToken, burger)
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("name", notNullValue())
                .body("order.number", notNullValue())
                .body("order.ingredients", hasSize(burger.getIngredients().size()))
                .body("order.owner.email", equalTo(user.getEmail()));
    }

    @Test
    @DisplayName("Неавторизованный пользователь может создать заказ с ингредиентами")
    @Description("Запрос без токена возвращает 200 и номер заказа, но без данных о владельце")
    void unauthorizedUserCanCreateOrderWithIngredients() {
        orderClient.createWithoutAuth(burger)
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("name", notNullValue())
                .body("order.number", notNullValue())
                .body("order.owner", nullValue());
    }

    @Test
    @DisplayName("Нельзя создать заказ без ингредиентов")
    @Description("Запрос с пустым списком ингредиентов возвращает 400")
    void orderCannotBeCreatedWithoutIngredients() {
        orderClient.create(accessToken, OrderGenerator.withoutIngredients())
                .statusCode(SC_BAD_REQUEST)
                .body("success", equalTo(false))
                .body("message", equalTo(NO_INGREDIENTS_MESSAGE));
    }

    @Test
    @DisplayName("Нельзя создать заказ с неверным хешем ингредиента")
    @Description("Запрос с невалидным id ингредиента возвращает 500")
    void orderCannotBeCreatedWithInvalidIngredientHash() {
        orderClient.create(accessToken, OrderGenerator.withInvalidHash())
                .statusCode(SC_INTERNAL_SERVER_ERROR)
                .body(containsString(INTERNAL_SERVER_ERROR));
    }
}

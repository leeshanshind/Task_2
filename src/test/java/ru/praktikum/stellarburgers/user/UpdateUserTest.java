package ru.praktikum.stellarburgers.user;

import io.qameta.allure.Description;
import io.qameta.allure.junit5.AllureJunit5;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.praktikum.stellarburgers.client.UserClient;
import ru.praktikum.stellarburgers.generator.UserGenerator;
import ru.praktikum.stellarburgers.model.User;
import ru.praktikum.stellarburgers.model.UserCredentials;

import java.util.Map;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(AllureJunit5.class)
@DisplayName("Изменение данных пользователя: PATCH /api/auth/user")
class UpdateUserTest {
    private static final String UNAUTHORISED_MESSAGE = "You should be authorised";
    private static final String EMAIL = "email";

    private final UserClient userClient = new UserClient();
    private User user;
    private String accessToken;

    @BeforeEach
    void setUp() {
        user = UserGenerator.randomUser();
        accessToken = userClient.register(user)
                .statusCode(SC_OK)
                .extract().path("accessToken");
    }

    @AfterEach
    void tearDown() {
        if (accessToken != null) {
            userClient.delete(accessToken);
        }
    }

    @ParameterizedTest(name = "поле {0}")
    @ValueSource(strings = {"email", "name"})
    @DisplayName("Авторизованный пользователь может изменить email и имя")
    @Description("Запрос с токеном возвращает 200 и пользователя с новым значением поля")
    void authorizedUserCanUpdateField(String field) {
        String newValue = EMAIL.equals(field) ? UserGenerator.randomEmail() : UserGenerator.randomString();

        userClient.update(accessToken, Map.of(field, newValue))
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("user." + field, equalTo(newValue));
    }

    @Test
    @DisplayName("Авторизованный пользователь может изменить пароль")
    @Description("После смены пароля с токеном можно залогиниться с новым паролем")
    void authorizedUserCanUpdatePassword() {
        String newPassword = UserGenerator.randomString();

        userClient.update(accessToken, Map.of("password", newPassword))
                .statusCode(SC_OK)
                .body("success", equalTo(true));

        userClient.login(new UserCredentials(user.getEmail(), newPassword))
                .statusCode(SC_OK)
                .body("success", equalTo(true));
    }

    @ParameterizedTest(name = "поле {0}")
    @ValueSource(strings = {"email", "name", "password"})
    @DisplayName("Неавторизованный пользователь не может изменить данные")
    @Description("Запрос без токена возвращает 401, данные пользователя не меняются")
    void unauthorizedUserCannotUpdateField(String field) {
        String newValue = EMAIL.equals(field) ? UserGenerator.randomEmail() : UserGenerator.randomString();

        userClient.updateWithoutAuth(Map.of(field, newValue))
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo(UNAUTHORISED_MESSAGE));

        userClient.login(UserCredentials.from(user))
                .statusCode(SC_OK)
                .body("user.email", equalTo(user.getEmail()))
                .body("user.name", equalTo(user.getName()));
    }
}

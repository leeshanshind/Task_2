package ru.praktikum.stellarburgers.user;

import io.qameta.allure.Description;
import io.qameta.allure.junit5.AllureJunit5;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.praktikum.stellarburgers.client.UserClient;
import ru.praktikum.stellarburgers.generator.UserGenerator;
import ru.praktikum.stellarburgers.model.User;
import ru.praktikum.stellarburgers.model.UserCredentials;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

@ExtendWith(AllureJunit5.class)
@DisplayName("Логин пользователя: POST /api/auth/login")
class LoginUserTest {
    private static final String INCORRECT_CREDENTIALS_MESSAGE = "email or password are incorrect";

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

    @Test
    @DisplayName("Существующий пользователь может залогиниться")
    @Description("Логин с верными email и паролем возвращает 200, данные пользователя и токены")
    void existingUserCanLogin() {
        userClient.login(UserCredentials.from(user))
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("user.email", equalTo(user.getEmail()))
                .body("user.name", equalTo(user.getName()))
                .body("accessToken", startsWith("Bearer "))
                .body("refreshToken", notNullValue());
    }

    @ParameterizedTest(name = "неверный email: {0}, неверный пароль: {1}")
    @CsvSource({
            "true, false",
            "false, true",
            "true, true"
    })
    @DisplayName("Нельзя залогиниться с неверным логином или паролем")
    @Description("Логин с неверным email и/или паролем возвращает 401")
    void userCannotLoginWithWrongCredentials(boolean wrongEmail, boolean wrongPassword) {
        UserCredentials credentials = new UserCredentials(
                wrongEmail ? UserGenerator.randomEmail() : user.getEmail(),
                wrongPassword ? UserGenerator.randomString() : user.getPassword());

        userClient.login(credentials)
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo(INCORRECT_CREDENTIALS_MESSAGE));
    }
}

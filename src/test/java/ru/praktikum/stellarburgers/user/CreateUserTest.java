package ru.praktikum.stellarburgers.user;

import io.qameta.allure.Description;
import io.qameta.allure.junit5.AllureJunit5;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.praktikum.stellarburgers.client.UserClient;
import ru.praktikum.stellarburgers.generator.UserGenerator;
import ru.praktikum.stellarburgers.model.User;

import java.util.stream.Stream;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(AllureJunit5.class)
@DisplayName("Создание пользователя: POST /api/auth/register")
class CreateUserTest {
    private static final String USER_EXISTS_MESSAGE = "User already exists";
    private static final String REQUIRED_FIELDS_MESSAGE = "Email, password and name are required fields";

    private final UserClient userClient = new UserClient();
    private String accessToken;

    @AfterEach
    void tearDown() {
        if (accessToken != null) {
            userClient.delete(accessToken);
        }
    }

    @Test
    @DisplayName("Можно создать уникального пользователя")
    @Description("Регистрация с новыми email, паролем и именем возвращает 200, данные пользователя и токены")
    void uniqueUserCanBeCreated() {
        User user = UserGenerator.randomUser();

        accessToken = userClient.register(user)
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("user.email", equalTo(user.getEmail()))
                .body("user.name", equalTo(user.getName()))
                .body("accessToken", startsWith("Bearer "))
                .body("refreshToken", notNullValue())
                .extract().path("accessToken");
    }

    @Test
    @DisplayName("Нельзя создать пользователя, который уже зарегистрирован")
    @Description("Повторная регистрация с тем же email возвращает 403")
    void existingUserCannotBeCreated() {
        User user = UserGenerator.randomUser();
        accessToken = userClient.register(user)
                .statusCode(SC_OK)
                .extract().path("accessToken");

        userClient.register(user)
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo(USER_EXISTS_MESSAGE));
    }

    static Stream<Arguments> usersWithoutRequiredField() {
        return Stream.of(
                arguments("email", new User(null, UserGenerator.randomString(), UserGenerator.randomString())),
                arguments("password", new User(UserGenerator.randomEmail(), null, UserGenerator.randomString())),
                arguments("name", new User(UserGenerator.randomEmail(), UserGenerator.randomString(), null))
        );
    }

    @ParameterizedTest(name = "без поля {0}")
    @MethodSource("usersWithoutRequiredField")
    @DisplayName("Нельзя создать пользователя без обязательного поля")
    @Description("Регистрация без email, пароля или имени возвращает 403")
    void userWithoutRequiredFieldCannotBeCreated(String missingField, User user) {
        userClient.register(user)
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo(REQUIRED_FIELDS_MESSAGE));
    }
}

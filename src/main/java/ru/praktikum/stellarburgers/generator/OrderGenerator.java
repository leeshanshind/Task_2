package ru.praktikum.stellarburgers.generator;

import io.restassured.response.ValidatableResponse;
import ru.praktikum.stellarburgers.model.Order;

import java.util.List;

public class OrderGenerator {
    private static final String BUN = "bun";
    private static final String FILLING = "main";
    private static final String SAUCE = "sauce";
    private static final String INVALID_HASH = "0000000000000000invalid0";

    private OrderGenerator() {
    }

    // стенд не создаёт заказ без булки (500), поэтому бургер всегда собираем с булкой
    public static Order burger(ValidatableResponse ingredients) {
        return new Order(List.of(
                idOfType(ingredients, BUN),
                idOfType(ingredients, FILLING),
                idOfType(ingredients, SAUCE)));
    }

    public static Order withoutIngredients() {
        return new Order(List.of());
    }

    public static Order withInvalidHash() {
        return new Order(List.of(INVALID_HASH));
    }

    private static String idOfType(ValidatableResponse ingredients, String type) {
        return ingredients.extract().path(String.format("data.find { it.type == '%s' }._id", type));
    }
}

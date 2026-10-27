package ru.praktikum.stellarburgers.model;

import java.util.List;

public class Order {
    private final List<String> ingredients;

    public Order(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public List<String> getIngredients() {
        return ingredients;
    }
}

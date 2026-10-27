package ru.praktikum.stellarburgers.generator;

import ru.praktikum.stellarburgers.model.User;

import java.util.UUID;

public class UserGenerator {
    private static final String EMAIL_DOMAIN = "@example.com";
    private static final int RANDOM_PART_LENGTH = 10;

    private UserGenerator() {
    }

    public static User randomUser() {
        return new User(randomEmail(), randomString(), randomString());
    }

    public static String randomEmail() {
        return "user_" + randomString() + EMAIL_DOMAIN;
    }

    public static String randomString() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, RANDOM_PART_LENGTH);
    }
}

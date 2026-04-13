package com.RBAC;

public record User(String username, String fullName, String email) {

    public static User validate(String username, String fullName, String email) {
        ValidationUtils.requireNonEmpty(username, "username");
        ValidationUtils.requireNonEmpty(fullName, "fullName");
        ValidationUtils.requireNonEmpty(email, "email");

        String normalizedUsername = ValidationUtils.normalizeString(username);
        String normalizedFullName = ValidationUtils.normalizeString(fullName);
        String normalizedEmail = ValidationUtils.normalizeString(email);

        if (!ValidationUtils.isValidUsername(normalizedUsername)) {
            throw new IllegalArgumentException(
                    "Имя пользователя должно содержать только латинские буквы, " +
                            "цифры и подчёркивание и должно быть от 3 до 20 символов"
            );
        }

        if (!ValidationUtils.isValidEmail(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "Email должен быть в корректном формате (например, user@example.com)"
            );
        }

        return new User(normalizedUsername, normalizedFullName, normalizedEmail);
    }

    public String format() {
        return String.format("%s (%s) <%s>", username, fullName, email);
    }
}
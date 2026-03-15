package com.RBAC;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "^\\d{4}-\\d{2}-\\d{2}$|^\\d{2}\\.\\d{2}\\.\\d{4}$|^\\d{2}/\\d{2}/\\d{4}$"
    );

    public static boolean isValidUsername(String username) {
        if (username == null) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidDate(String date) {
        if (date == null) {
            return false;
        }
        return DATE_PATTERN.matcher(date).matches();
    }

    public static String normalizeString(String input) {
        if (input == null) {
            return null;
        }

        String trimmed = input.trim();

        String singleSpaced = trimmed.replaceAll("\\s+", " ");

        return singleSpaced.toLowerCase();
    }

    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Поле '%s' не должно быть пустым или null", fieldName)
            );
        }
    }
}
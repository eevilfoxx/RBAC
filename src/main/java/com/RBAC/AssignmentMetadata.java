package com.RBAC;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    public AssignmentMetadata {

        if (assignedBy.isEmpty()) {
            throw new IllegalArgumentException("Имя назначившего не может быть пустым");
        }

        if (!USERNAME_PATTERN.matcher(assignedBy).matches()) {
            throw new IllegalArgumentException("Имя назначившего должно содержать только латинские буквы, цифры и подчёркивание и должно быть от 3 до 20 символов");
        }

        if (assignedAt.isEmpty()) {
            throw new IllegalArgumentException("Дата назначения не может быть пустой");
        }

        if (reason != null && reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Причина назначения не может быть пустой строкой");
        }
    }

    public static AssignmentMetadata now(String assignedBy, String reason) {
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return new AssignmentMetadata(assignedBy, currentDateTime, reason);
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Assigned by: %s\n", assignedBy));
        sb.append(String.format("Assigned at: %s\n", assignedAt));

        if (reason() != null) {
            sb.append(String.format("Reason: %s", reason));
        } else {
            sb.append("Reason: Not specified");
        }

        return sb.toString();
    }

    public String getReason() {
        return reason;
    }
}

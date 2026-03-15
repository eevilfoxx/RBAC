package com.RBAC;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {

    private static final java.util.regex.Pattern USERNAME_PATTERN =
            java.util.regex.Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

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

        if (!DateUtils.isValidDate(assignedAt)) {
            throw new IllegalArgumentException("Неверный формат даты. Используйте YYYY-MM-DD");
        }

        if (reason != null && reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Причина назначения не может быть пустой строкой");
        }
    }

    public static AssignmentMetadata now(String assignedBy, String reason) {
        String currentDate = DateUtils.getCurrentDate();
        return new AssignmentMetadata(assignedBy, currentDate, reason);
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Назначил: %s\n", assignedBy));
        sb.append(String.format("Дата назначения: %s", assignedAt));

        if (reason != null && !reason.isEmpty()) {
            sb.append(String.format(" (%s)", DateUtils.formatRelativeTime(assignedAt)));
        }
        sb.append("\n");

        if (reason() != null && !reason().isEmpty()) {
            sb.append(String.format("Причина: %s", reason));
        } else {
            sb.append("Причина: не указана");
        }

        return sb.toString();
    }

    public String getReason() {
        return reason;
    }

    public String getRelativeTime() {
        return DateUtils.formatRelativeTime(assignedAt);
    }

    public boolean isToday() {
        return DateUtils.isEqual(assignedAt, DateUtils.getCurrentDate());
    }

    public boolean isThisWeek() {
        String weekAgo = DateUtils.subtractDays(DateUtils.getCurrentDate(), 7);
        return DateUtils.isAfterOrEqual(assignedAt, weekAgo);
    }
}
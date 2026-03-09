package com.RBAC;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class TemporaryAssignment extends AbstractRoleAssignment {
    String expiresAt;
    private boolean autoRenew;

    TemporaryAssignment(User user, Role role, AssignmentMetadata metadata) {
        super(user, role, metadata);
    }

    @Override
    public boolean isActive() {
        return !isExpired();
    }

    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    public void extend(String newExpirationDate) {
        LocalDateTime currentExpiration = LocalDate.parse(expiresAt, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .atStartOfDay();
        LocalDateTime newExpiration;

        try {
            newExpiration = LocalDate.parse(newExpirationDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .atStartOfDay();
        } catch (DateTimeParseException e) {
            newExpiration = LocalDate.parse(newExpirationDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .atStartOfDay();
        }

        this.expiresAt = newExpirationDate;
    }

    public boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = LocalDate.parse(expiresAt, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .atStartOfDay();
        return now.isAfter(expiration);
    }

    public String getTimeRemaining() {
        if (isExpired()) {
            return "Expired";
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = LocalDate.parse(expiresAt, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .atStartOfDay();

        long days = ChronoUnit.DAYS.between(now, expiration);
        long hours = ChronoUnit.HOURS.between(now, expiration) % 24;
        long minutes = ChronoUnit.MINUTES.between(now, expiration) % 60;

        if (days > 0) {
            return String.format("%d days, %d hours, %d minutes", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%d hours, %d minutes", hours, minutes);
        } else {
            return String.format("%d minutes", minutes);
        }
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder(super.summary());

        sb.append(String.format("\nExpires at: %s", expiresAt));

        if (!isExpired()) {
            sb.append(String.format("\nTime remaining: %s", getTimeRemaining()));
        } else {
            sb.append("\nStatus: EXPIRED");
        }

        return sb.toString();
    }
}
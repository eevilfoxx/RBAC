package com.RBAC;

import java.time.LocalDate;
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
        // Просто обновляем expiresAt
        this.expiresAt = newExpirationDate;
    }

    public boolean isExpired() {
        LocalDate now = LocalDate.now();
        LocalDate expiration = LocalDate.parse(expiresAt); // yyyy-MM-dd
        return now.isAfter(expiration);
    }

    public String getTimeRemaining() {
        if (isExpired()) {
            return "Expired";
        }

        LocalDate now = LocalDate.now();
        LocalDate expiration = LocalDate.parse(expiresAt);
        long days = ChronoUnit.DAYS.between(now, expiration);

        if (days > 0) {
            return String.format("%d days", days);
        } else {
            return "Less than 1 day";
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
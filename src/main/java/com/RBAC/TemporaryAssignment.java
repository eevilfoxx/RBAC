package com.RBAC;

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
        if (!DateUtils.isValidDate(newExpirationDate)) {
            throw new IllegalArgumentException("Неверный формат даты. Используйте YYYY-MM-DD");
        }

        if (expiresAt != null && DateUtils.isBeforeOrEqual(newExpirationDate, expiresAt)) {
            throw new IllegalArgumentException("Новая дата должна быть позже текущей");
        }

        this.expiresAt = newExpirationDate;
    }

    public boolean isExpired() {
        if (expiresAt == null) return false;
        return DateUtils.isBefore(expiresAt, DateUtils.getCurrentDate());
    }

    public String getTimeRemaining() {
        if (expiresAt == null) return "Бессрочно";

        if (isExpired()) {
            long daysAgo = DateUtils.getDaysSince(expiresAt);
            return String.format("Истекло %d дн. назад", daysAgo);
        }

        long daysLeft = DateUtils.getDaysUntil(expiresAt);

        if (daysLeft == 0) {
            return "Истекает сегодня";
        } else if (daysLeft < 0) {
            return "Просрочено";
        } else if (daysLeft == 1) {
            return "1 день";
        } else if (daysLeft <= 30) {
            return daysLeft + " дней";
        } else {
            long monthsLeft = daysLeft / 30;
            long remainingDays = daysLeft % 30;
            if (remainingDays == 0) {
                return monthsLeft + " мес.";
            } else {
                return monthsLeft + " мес. " + remainingDays + " дн.";
            }
        }
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        if (!DateUtils.isValidDate(expiresAt)) {
            throw new IllegalArgumentException("Неверный формат даты. Используйте YYYY-MM-DD");
        }
        this.expiresAt = expiresAt;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder(super.summary());

        sb.append(String.format("\nДата истечения: %s", expiresAt != null ? expiresAt : "не установлена"));

        if (expiresAt != null) {
            sb.append(String.format(" (%s)", DateUtils.formatRelativeTime(expiresAt)));
        }

        if (!isExpired()) {
            sb.append(String.format("\nОсталось времени: %s", getTimeRemaining()));
        } else {
            sb.append(String.format("\nСтатус: ИСТЕКЛО (%s)", getTimeRemaining()));
        }

        if (autoRenew) {
            sb.append("\nАвтопродление: ВКЛ");
        }

        return sb.toString();
    }

    public String getStatusWithIcon() {
        return DateUtils.getStatusWithIcon(expiresAt);
    }

    public long getDaysUntilExpiration() {
        return expiresAt != null ? DateUtils.getDaysUntil(expiresAt) : Long.MAX_VALUE;
    }

    public long getDaysSinceExpiration() {
        return expiresAt != null && isExpired() ? DateUtils.getDaysSince(expiresAt) : 0;
    }
}
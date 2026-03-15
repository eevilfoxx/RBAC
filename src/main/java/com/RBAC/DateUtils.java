package com.RBAC;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMAT);
    }

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMAT);
    }

    public static boolean isBefore(String date1, String date2) {
        if (date1 == null || date2 == null) return false;
        return date1.compareTo(date2) < 0;
    }

    public static boolean isAfter(String date1, String date2) {
        if (date1 == null || date2 == null) return false;
        return date1.compareTo(date2) > 0;
    }

    public static boolean isBeforeOrEqual(String date1, String date2) {
        if (date1 == null || date2 == null) return false;
        return date1.compareTo(date2) <= 0;
    }

    public static boolean isAfterOrEqual(String date1, String date2) {
        if (date1 == null || date2 == null) return false;
        return date1.compareTo(date2) >= 0;
    }

    public static boolean isEqual(String date1, String date2) {
        if (date1 == null || date2 == null) return false;
        return date1.equals(date2);
    }

    public static String addDays(String date, int days) {
        if (date == null) return null;
        try {
            LocalDate ld = LocalDate.parse(date, DATE_FORMAT);
            return ld.plusDays(days).format(DATE_FORMAT);
        } catch (Exception e) {
            return date;
        }
    }

    public static String subtractDays(String date, int days) {
        return addDays(date, -days);
    }

    public static String formatRelativeTime(String date) {
        if (date == null) return "неизвестно";

        try {
            LocalDate targetDate = LocalDate.parse(date, DATE_FORMAT);
            LocalDate today = LocalDate.now();

            long daysDiff = ChronoUnit.DAYS.between(today, targetDate);

            if (daysDiff == 0) {
                return "сегодня";
            } else if (daysDiff == 1) {
                return "завтра";
            } else if (daysDiff == -1) {
                return "вчера";
            } else if (daysDiff > 1) {
                return "через " + daysDiff + " дн.";
            } else {
                long absDays = Math.abs(daysDiff);
                return absDays + " дн. назад";
            }
        } catch (Exception e) {
            return date;
        }
    }

    public static String formatDateTimeRelative(String dateTime) {
        if (dateTime == null) return "неизвестно";

        try {
            LocalDateTime targetDateTime = LocalDateTime.parse(dateTime, DATETIME_FORMAT);
            LocalDateTime now = LocalDateTime.now();

            long minutesDiff = ChronoUnit.MINUTES.between(targetDateTime, now);
            long hoursDiff = ChronoUnit.HOURS.between(targetDateTime, now);
            long daysDiff = ChronoUnit.DAYS.between(targetDateTime.toLocalDate(), now.toLocalDate());

            if (minutesDiff < 1) {
                return "только что";
            } else if (minutesDiff < 60) {
                return minutesDiff + " мин. назад";
            } else if (hoursDiff < 24) {
                return hoursDiff + " ч. назад";
            } else if (daysDiff == 1) {
                return "вчера";
            } else if (daysDiff < 7) {
                return daysDiff + " дн. назад";
            } else {
                return targetDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            }
        } catch (Exception e) {
            return dateTime;
        }
    }

    public static boolean isValidDate(String date) {
        if (date == null) return false;
        try {
            LocalDate.parse(date, DATE_FORMAT);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String parseDate(String dateStr) {
        if (dateStr == null) return null;
        try {
            // Пробуем разные форматы
            if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return dateStr;
            } else if (dateStr.matches("\\d{2}\\.\\d{2}\\.\\d{4}")) {
                String[] parts = dateStr.split("\\.");
                return parts[2] + "-" + parts[1] + "-" + parts[0];
            } else if (dateStr.matches("\\d{2}/\\d{2}/\\d{4}")) {
                String[] parts = dateStr.split("/");
                return parts[2] + "-" + parts[0] + "-" + parts[1];
            }
            return dateStr;
        } catch (Exception e) {
            return dateStr;
        }
    }

    public static String getStatusWithIcon(String expiresAt) {
        if (expiresAt == null) return "Постоянное";

        try {
            LocalDate expiryDate = LocalDate.parse(expiresAt, DATE_FORMAT);
            LocalDate today = LocalDate.now();

            if (expiryDate.isBefore(today)) {
                long daysAgo = ChronoUnit.DAYS.between(expiryDate, today);
                return "Истекло (" + daysAgo + " дн. назад)";
            } else if (expiryDate.isEqual(today)) {
                return "Истекает сегодня";
            } else {
                long daysLeft = ChronoUnit.DAYS.between(today, expiryDate);
                if (daysLeft <= 3) {
                    return "Истекает через " + daysLeft + " дн.";
                } else {
                    return "Действует до " + expiresAt;
                }
            }
        } catch (Exception e) {
            return expiresAt;
        }
    }

    public static long getDaysUntil(String date) {
        if (date == null) return 0;
        try {
            LocalDate targetDate = LocalDate.parse(date, DATE_FORMAT);
            LocalDate today = LocalDate.now();
            return ChronoUnit.DAYS.between(today, targetDate);
        } catch (Exception e) {
            return 0;
        }
    }

    public static long getDaysSince(String date) {
        if (date == null) return 0;
        try {
            LocalDate targetDate = LocalDate.parse(date, DATE_FORMAT);
            LocalDate today = LocalDate.now();
            return ChronoUnit.DAYS.between(targetDate, today);
        } catch (Exception e) {
            return 0;
        }
    }
}
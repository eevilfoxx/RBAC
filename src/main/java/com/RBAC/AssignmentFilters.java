package com.RBAC;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class AssignmentFilters {

    private AssignmentFilters() {}

    public static AssignmentFilter byUser(User user) {
        return a -> a != null && a.user().equals(user);
    }

    public static AssignmentFilter byUsername(String username) {
        return a -> a != null && a.user().username().equals(username);
    }

    public static AssignmentFilter byRole(Role role) {
        return a -> a != null && a.role().equals(role);
    }

    public static AssignmentFilter byRoleName(String roleName) {
        return a -> a != null && a.role().getName().equals(roleName);
    }

    public static AssignmentFilter activeOnly() {
        return a -> a != null && a.isActive();
    }

    public static AssignmentFilter inactiveOnly() {
        return a -> a != null && !a.isActive();
    }

    public static AssignmentFilter byType(String type) {
        return a -> a != null && a.assignmentType().equalsIgnoreCase(type);
    }

    public static AssignmentFilter assignedBy(String username) {
        return a -> a != null && a.metadata().assignedBy().equals(username);
    }

    public static AssignmentFilter assignedAfter(String date) {
        try {
            LocalDate targetDate = LocalDate.parse(date);
            return a -> {
                if (a == null || a.metadata() == null || a.metadata().assignedAt() == null) {
                    return false;
                }

                try {
                    LocalDateTime assignmentDateTime = LocalDateTime.parse(
                            a.metadata().assignedAt(),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    );
                    return assignmentDateTime.toLocalDate().isAfter(targetDate);
                } catch (DateTimeParseException e) {
                    return false;
                }
            };
        } catch (DateTimeParseException e) {
            return a -> false;
        }
    }


    public static AssignmentFilter expiringBefore(String date) {
        try {
            LocalDate targetDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return a -> {
                if (a == null) {
                    return false;
                }

                if (!(a instanceof TemporaryAssignment)) {
                    return false;
                }

                TemporaryAssignment tempAssignment = (TemporaryAssignment) a;
                String expiresAt = tempAssignment.expiresAt;

                if (expiresAt == null || expiresAt.isEmpty()) {
                    return false;
                }

                try {
                    LocalDate expirationDate = LocalDate.parse(expiresAt, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    return expirationDate.isBefore(targetDate);
                } catch (DateTimeParseException e) {
                    return false;
                }
            };
        } catch (DateTimeParseException e) {
            return a -> false;
        }
    }

    private final Map<String, RoleAssignment> assignments = new ConcurrentHashMap<>();

    public List<RoleAssignment> findByFilterParallel(AssignmentFilter filter) {
    return assignments.values().parallelStream()
            .filter(filter::test)
            .collect(Collectors.toList());
    }

}
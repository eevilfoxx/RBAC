package com.RBAC;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;

public final class AssignmentSorters {

    private AssignmentSorters() {}

    public static Comparator<RoleAssignment> byUsername() {
        return Comparator.comparing(a -> a.user().username());
    }

    public static Comparator<RoleAssignment> byRoleName() {
        return Comparator.comparing(a -> a.role().getName());
    }

    public static Comparator<RoleAssignment> byAssignmentDate() {
        return Comparator.comparing(
                a -> {
                    if (a == null || a.metadata() == null || a.metadata().assignedAt() == null) {
                        return LocalDateTime.MIN;
                    }
                    try {
                        return LocalDateTime.parse(a.metadata().assignedAt(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    } catch (DateTimeParseException e) {
                        return LocalDateTime.MIN;
                    }
                },
                Comparator.nullsLast(LocalDateTime::compareTo)
        );
    }
}
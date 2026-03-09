package com.RBAC;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment> {

    private final Map<String, RoleAssignment> assignments = new HashMap<>();

    @Override
    public void add(RoleAssignment assignment) {
        boolean duplicate = assignments.values().stream()
                .anyMatch(a ->
                        a.isActive() &&
                                a.user().equals(assignment.user()) &&
                                a.role().equals(assignment.role()));

        if (duplicate) {
            throw new IllegalStateException("Role already assigned to user");
        }

        assignments.put(assignment.assignmentId(), assignment);
    }

    @Override
    public boolean remove(RoleAssignment assignment) {
        return assignments.remove(assignment.assignmentId()) != null;
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        return Optional.ofNullable(assignments.get(id));
    }

    @Override
    public List<RoleAssignment> findAll() {
        return new ArrayList<>(assignments.values());
    }

    @Override
    public int count() {
        return assignments.size();
    }

    @Override
    public void clear() {
        assignments.clear();
    }

    public List<RoleAssignment> findByUser(User user) {
        return assignments.values().stream()
                .filter(a -> a.user().equals(user))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByRole(Role role) {
        return assignments.values().stream()
                .filter(a -> a.role().equals(role))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        return assignments.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        return assignments.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getActiveAssignments() {
        return assignments.values().stream()
                .filter(RoleAssignment::isActive)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getExpiredAssignments() {
        LocalDateTime now = LocalDateTime.now();
        return assignments.values().stream()
                .filter(a -> a instanceof TemporaryAssignment)
                .map(a -> (TemporaryAssignment) a)
                .filter(a -> {
                    try {
                        LocalDateTime expirationDate = LocalDateTime.parse(a.expiresAt, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        return expirationDate.isBefore(now);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }


    public boolean userHasRole(User user, Role role) {
        return assignments.values().stream()
                .anyMatch(a ->
                        a.isActive() &&
                                a.user().equals(user) &&
                                a.role().equals(role));
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        return getUserPermissions(user).stream()
                .anyMatch(p ->
                        p.name().equals(permissionName) &&
                                p.resource().equals(resource));
    }

    public Set<Permission> getUserPermissions(User user) {
        return assignments.values().stream()
                .filter(a -> a.isActive() && a.user().equals(user))
                .flatMap(a -> a.role().getPermissions().stream())
                .collect(Collectors.toSet());
    }

    public void revokeAssignment(String assignmentId) {
        RoleAssignment a = assignments.get(assignmentId);
        if (a == null) {
            throw new NoSuchElementException("Assignment not found");
        }

        if (a instanceof TemporaryAssignment) {
            String extendDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            ((TemporaryAssignment) a).extend(extendDate);
        } else {
            assignments.remove(assignmentId);
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        RoleAssignment a = assignments.get(assignmentId);
        if (a == null) {
            throw new NoSuchElementException("Assignment not found");
        }

        if (!(a instanceof TemporaryAssignment)) {
            throw new IllegalArgumentException("Assignment is not temporary");
        }

        TemporaryAssignment tempAssignment = (TemporaryAssignment) a;
        tempAssignment.extend(newExpirationDate);
    }
}
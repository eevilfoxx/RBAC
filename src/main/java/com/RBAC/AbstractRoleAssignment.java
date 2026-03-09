package com.RBAC;

import java.util.Objects;
import java.util.UUID;

abstract class AbstractRoleAssignment implements RoleAssignment {
    String assignmentId;
    User user;
    Role role;
    AssignmentMetadata metadata;

    public AbstractRoleAssignment(User user, Role role, AssignmentMetadata metadata) {

        this.assignmentId = "assign_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        this.user = user;
        this.role = role;
        this.metadata = metadata;
    }

    @Override
    public String assignmentId() {
        return assignmentId;
    }

    @Override
    public User user() {
        return user;
    }

    @Override
    public Role role() {
        return role;
    }

    @Override
    public AssignmentMetadata metadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRoleAssignment abstractRoleAssignment = (AbstractRoleAssignment) o;
        return Objects.equals(assignmentId, abstractRoleAssignment.assignmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentId);
    }

    public abstract boolean isActive();
    public abstract String assignmentType();

    public String getAssignmentId() {
        return assignmentId;
    }

    public User getUser() {
        return user;
    }

    public Role getRole() {
        return role;
    }

    public AssignmentMetadata getMetadata() {
        return metadata;
    }

    public String summary() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("[%s] %s assigned to %s by %s at %s\n",
                assignmentType().toUpperCase(),
                role.getName(),
                user.username(),
                metadata.assignedBy(),
                metadata.assignedAt()));

        sb.append(String.format("Reason: %s\n", metadata.getReason()));
        sb.append(String.format("Status: %s", isActive()));

        return sb.toString();
    }

}

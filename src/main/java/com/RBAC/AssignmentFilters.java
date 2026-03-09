import java.time.LocalDate;

public final class AssignmentFilters {

    private AssignmentFilters() {}

    public static AssignmentFilter byUser(User user) {
        return a -> a != null && a.getUser().equals(user);
    }

    public static AssignmentFilter byUsername(String username) {
        return a -> a != null && a.getUser().getUsername().equals(username);
    }

    public static AssignmentFilter byRole(Role role) {
        return a -> a != null && a.getRole().equals(role);
    }

    public static AssignmentFilter byRoleName(String roleName) {
        return a -> a != null && a.getRole().getName().equals(roleName);
    }

    public static AssignmentFilter activeOnly() {
        return a -> a != null && a.isActive();
    }

    public static AssignmentFilter inactiveOnly() {
        return a -> a != null && !a.isActive();
    }

    public static AssignmentFilter byType(String type) {
        return a -> a != null && a.getType().equalsIgnoreCase(type);
    }

    public static AssignmentFilter assignedBy(String username) {
        return a -> a != null && a.getAssignedBy().equals(username);
    }

    public static AssignmentFilter assignedAfter(String date) {
        LocalDate d = LocalDate.parse(date);
        return a -> a != null && a.getAssignedAt().isAfter(d);
    }

    public static AssignmentFilter expiringBefore(String date) {
        LocalDate d = LocalDate.parse(date);
        return a -> a != null
                && a.getExpiresAt() != null
                && a.getExpiresAt().isBefore(d);
    }
}
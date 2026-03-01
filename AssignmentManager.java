import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment> {

    private final Map<String, RoleAssignment> assignments = new HashMap<>();

    @Override
    public void add(RoleAssignment assignment) {
        boolean duplicate = assignments.values().stream()
                .anyMatch(a ->
                        a.isActive() &&
                                a.getUser().equals(assignment.getUser()) &&
                                a.getRole().equals(assignment.getRole()));

        if (duplicate) {
            throw new IllegalStateException("Role already assigned to user");
        }

        assignments.put(assignment.getId(), assignment);
    }

    @Override
    public boolean remove(RoleAssignment assignment) {
        return assignments.remove(assignment.getId()) != null;
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
                .filter(a -> a.getUser().equals(user))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByRole(Role role) {
        return assignments.values().stream()
                .filter(a -> a.getRole().equals(role))
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
        LocalDate now = LocalDate.now();
        return assignments.values().stream()
                .filter(a -> a.getExpiresAt() != null && a.getExpiresAt().isBefore(now))
                .collect(Collectors.toList());
    }

    public boolean userHasRole(User user, Role role) {
        return assignments.values().stream()
                .anyMatch(a ->
                        a.isActive() &&
                                a.getUser().equals(user) &&
                                a.getRole().equals(role));
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        return getUserPermissions(user).stream()
                .anyMatch(p ->
                        p.getName().equals(permissionName) &&
                                p.getResource().equals(resource));
    }

    public Set<Permission> getUserPermissions(User user) {
        return assignments.values().stream()
                .filter(a -> a.isActive() && a.getUser().equals(user))
                .flatMap(a -> a.getRole().getPermissions().stream())
                .collect(Collectors.toSet());
    }

    public void revokeAssignment(String assignmentId) {
        RoleAssignment a = assignments.get(assignmentId);
        if (a == null) {
            throw new NoSuchElementException("Assignment not found");
        }
        a.deactivate();
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        RoleAssignment a = assignments.get(assignmentId);
        if (a == null) {
            throw new NoSuchElementException("Assignment not found");
        }
        a.extend(LocalDate.parse(newExpirationDate));
    }
}
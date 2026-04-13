import java.util.concurrent.ConcurrentHashMap;

public class AssignmentManager implements Repository<RoleAssignment> {

    private final ConcurrentHashMap<String, RoleAssignment> assignments = new ConcurrentHashMap<>();

    @Override
    public void add(RoleAssignment assignment) {
        synchronized (this) {
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
    }

    @Override
    public boolean remove(RoleAssignment assignment) {
        return assignments.remove(assignment.assignmentId(), assignment);
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
        return assignments.values().stream()
                .filter(a -> a instanceof TemporaryAssignment)
                .map(a -> (TemporaryAssignment) a)
                .filter(TemporaryAssignment::isExpired)
                .collect(Collectors.toList());
    }

    public List<TemporaryAssignment> getAssignmentsExpiringSoon(int daysThreshold) {
        String thresholdDate = DateUtils.addDays(DateUtils.getCurrentDate(), daysThreshold);

        return assignments.values().stream()
                .filter(a -> a instanceof TemporaryAssignment)
                .map(a -> (TemporaryAssignment) a)
                .filter(ta -> ta.getExpiresAt() != null)
                .filter(ta -> !ta.isExpired())
                .filter(ta -> DateUtils.isBeforeOrEqual(ta.getExpiresAt(), thresholdDate))
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
        assignments.compute(assignmentId, (id, a) -> {
            if (a == null) {
                throw new NoSuchElementException("Assignment not found");
            }

            if (a instanceof TemporaryAssignment temp) {
                String yesterday = DateUtils.subtractDays(DateUtils.getCurrentDate(), 1);
                temp.setExpiresAt(yesterday);
                return temp;
            } else {
                return null; // удаление
            }
        });
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        assignments.compute(assignmentId, (id, a) -> {
            if (a == null) {
                throw new NoSuchElementException("Assignment not found");
            }

            if (!(a instanceof TemporaryAssignment temp)) {
                throw new IllegalArgumentException("Assignment is not temporary");
            }

            if (!DateUtils.isValidDate(newExpirationDate)) {
                throw new IllegalArgumentException("Invalid date format");
            }

            if (temp.getExpiresAt() != null &&
                    DateUtils.isBeforeOrEqual(newExpirationDate, temp.getExpiresAt())) {
                throw new IllegalArgumentException("New date must be later");
            }

            temp.extend(newExpirationDate);
            return temp;
        });
    }

    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();

        stats.put("total", (long) assignments.size());
        stats.put("active", (long) getActiveAssignments().size());
        stats.put("expired", (long) getExpiredAssignments().size());
        stats.put("expiringSoon", (long) getAssignmentsExpiringSoon(3).size());
        stats.put("temporary", assignments.values().stream()
                .filter(a -> a instanceof TemporaryAssignment)
                .count());
        stats.put("permanent", assignments.values().stream()
                .filter(a -> !(a instanceof TemporaryAssignment))
                .count());

        return stats;
    }

    public List<RoleAssignment> getAssignmentsByDateRange(String startDate, String endDate) {
        return assignments.values().stream()
                .filter(a -> {
                    String assignedAt = a.metadata().assignedAt();
                    return DateUtils.isAfterOrEqual(assignedAt, startDate) &&
                            DateUtils.isBeforeOrEqual(assignedAt, endDate);
                })
                .collect(Collectors.toList());
    }
}
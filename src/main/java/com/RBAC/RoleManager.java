import java.util.concurrent.ConcurrentHashMap;

public class RoleManager implements Repository<Role> {

    private final ConcurrentHashMap<String, Role> rolesById = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Role> rolesByName = new ConcurrentHashMap<>();

    @Override
    public void add(Role role) {
        Role existing = rolesByName.putIfAbsent(role.getName(), role);
        if (existing != null) {
            throw new IllegalArgumentException("Role name already exists: " + role.getName());
        }

        rolesById.put(role.getId(), role);
    }

    @Override
    public boolean remove(Role role) {
        synchronized (this) {
            Role removed = rolesById.remove(role.getId());
            if (removed != null) {
                rolesByName.remove(removed.getName());
                return true;
            }
            return false;
        }
    }

    @Override
    public Optional<Role> findById(String id) {
        return Optional.ofNullable(rolesById.get(id));
    }

    @Override
    public List<Role> findAll() {
        return new ArrayList<>(rolesById.values());
    }

    @Override
    public int count() {
        return rolesById.size();
    }

    @Override
    public void clear() {
        rolesById.clear();
        rolesByName.clear();
    }

    public Optional<Role> findByName(String name) {
        return Optional.ofNullable(rolesByName.get(name));
    }

    public List<Role> findByFilter(RoleFilter filter) {
        return rolesById.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        return rolesById.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public boolean exists(String name) {
        return rolesByName.containsKey(name);
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new NoSuchElementException("Role not found: " + roleName);
        }

        synchronized (role) {
            role.addPermission(permission);
        }
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new NoSuchElementException("Role not found: " + roleName);
        }

        synchronized (role) {
            role.getPermissions().remove(permission);
        }
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        return rolesById.values().stream()
                .filter(r -> r.getPermissions().stream()
                        .anyMatch(p ->
                                p.name().equals(permissionName) &&
                                p.resource().equals(resource)))
                .collect(Collectors.toList());
    }
}
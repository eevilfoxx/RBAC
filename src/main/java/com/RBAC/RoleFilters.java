package com.RBAC;

public final class RoleFilters {

    private RoleFilters() {}

    public static RoleFilter byName(String name) {
        return r -> r != null && r.getName().equals(name);
    }

    public static RoleFilter byNameContains(String substring) {
        String s = substring.toLowerCase();
        return r -> r != null && r.getName().toLowerCase().contains(s);
    }

    public static RoleFilter hasPermission(Permission permission) {
        return r -> r != null && r.getPermissions().contains(permission);
    }

    public static RoleFilter hasPermission(String permissionName, String resource) {
        return r -> r != null && r.getPermissions().stream()
                .anyMatch(p ->
                        p.name().equals(permissionName)
                                && p.resource().equals(resource));
    }

    public static RoleFilter hasAtLeastNPermissions(int n) {
        return r -> r != null && r.getPermissions().size() >= n;
    }

    public List<Role> findByFilterParallel(RoleFilter filter) {
    return rolesById.values().parallelStream()
            .filter(filter::test)
            .collect(Collectors.toList());
}
}
package com.RBAC;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

    private final Map<String, Role> rolesById = new ConcurrentHashMap<>();

    public List<Role> findByFilterParallel(RoleFilter filter) {
    return rolesById.values().parallelStream()
            .filter(filter::test)
            .collect(Collectors.toList());
}
}
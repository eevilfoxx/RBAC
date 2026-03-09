package com.RBAC;

import java.util.*;


public class Role {
    private final String id;
    private final String name;
    private final String description;
    private final Set<Permission> permissions;
    private static final Set<String> existingNames = new HashSet<>();

    public Role(String name, String description) {
        this.id = UUID.randomUUID().toString();
        if (existingNames.contains(name)) {
            throw new IllegalArgumentException("Роль с именем '" + name + "' уже существует!");
        }

        this.description = description;
        this.permissions = new HashSet<>();

        existingNames.add(name);
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Переопределение toString
    @Override
    public String toString() {
        return String.format("Role{id='%s', name='%s', description='%s', permissions=%s}",
                id, name, description, permissions);
    }

    public void addPermission(Permission permission) {
        if (permission != null) {
            permissions.add(permission);
        }
    }

    public void removePermission(Permission permission) {
        permissions.remove(permission);
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public boolean hasPermission(String permissionName, String resource) {
        return permissions.stream()
                .anyMatch(p -> p.name().equals(permissionName)
                        && p.resource().equals(resource));
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Role: %s [ID: %s]\n", name, id));
        sb.append(String.format("Description: %s\n", description));
        sb.append(String.format("Permissions (%d):\n", permissions.size()));

        int counter = 1;
        for (Permission permission : permissions) {
            sb.append(String.format("  %d. %s - %s\n",
                    counter++,
                    permission.name(),
                    permission.description()));
        }

        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return this.id;
    }
}
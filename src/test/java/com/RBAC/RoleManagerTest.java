package com.RBAC;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleManagerTest {

    private RoleManager manager;

    @Mock
    private Role role;

    @Mock
    private Permission permission;

    @BeforeEach
    void setUp() {
        manager = new RoleManager();
    }

    @Test
    void addRole() {

        when(role.getId()).thenReturn("1");
        when(role.getName()).thenReturn("ADMIN");

        manager.add(role);

        assertEquals(1, manager.count());
    }

    @Test
    void addPermissionToRole() {

        when(role.getId()).thenReturn("1");
        when(role.getName()).thenReturn("ADMIN");
        when(role.getPermissions()).thenReturn(new HashSet<>());

        manager.add(role);

        manager.addPermissionToRole("ADMIN", permission);

        assertTrue(role.getPermissions().contains(permission));
    }

    @Test
    void removePermissionFromRole() {

        HashSet<Permission> permissions = new HashSet<>();
        permissions.add(permission);

        when(role.getId()).thenReturn("1");
        when(role.getName()).thenReturn("ADMIN");
        when(role.getPermissions()).thenReturn(permissions);

        manager.add(role);

        manager.removePermissionFromRole("ADMIN", permission);

        assertFalse(permissions.contains(permission));
    }
}
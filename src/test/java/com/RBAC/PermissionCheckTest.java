package com.RBAC;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import static org.mockito.Mockito.*;

class PermissionCheckTest {

    @Test
    void checkUserPermission() {

        RBACSystem system = mock(RBACSystem.class);
        UserManager userManager = mock(UserManager.class);
        AssignmentManager assignmentManager = mock(AssignmentManager.class);

        when(system.getUserManager()).thenReturn(userManager);
        when(system.getAssignmentManager()).thenReturn(assignmentManager);

        User user = mock(User.class);
        RoleAssignment assignment = mock(RoleAssignment.class);
        Role role = mock(Role.class);

        when(userManager.findByUsername("john")).thenReturn(java.util.Optional.of(user));
        when(assignmentManager.findByUser(user)).thenReturn(List.of(assignment));
        when(assignment.role()).thenReturn(role);
        when(role.hasPermission("READ", "USERS")).thenReturn(true);

        Scanner scanner = new Scanner("""
                john
                READ
                USERS
                """);

        Command command = (s, sys) -> {

            String uname = s.nextLine();
            String pname = s.nextLine();
            String res = s.nextLine();

            User u = sys.getUserManager().findByUsername(uname).orElseThrow();

            Optional<RoleAssignment> r = sys.getAssignmentManager()
                    .findByUser(u)
                    .stream()
                    .filter(a -> a.role().hasPermission(pname, res))
                    .findFirst();

        };

        command.execute(scanner, system);

        verify(role).hasPermission("READ", "USERS");

    }

}
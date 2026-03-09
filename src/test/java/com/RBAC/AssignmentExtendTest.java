package com.RBAC;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Scanner;

import static org.mockito.Mockito.*;

class AssignmentExtendTest {

    @Test
    void extendTemporaryAssignment() {

        RBACSystem system = mock(RBACSystem.class);
        AssignmentManager manager = mock(AssignmentManager.class);

        when(system.getAssignmentManager()).thenReturn(manager);

        TemporaryAssignment assignment = mock(TemporaryAssignment.class);

        when(manager.findById("1")).thenReturn(Optional.of(assignment));

        Scanner scanner = new Scanner("""
                1
                2030-01-01
                """);

        Command command = (s, sys) -> {

            String id = s.nextLine();

            RoleAssignment ra = sys.getAssignmentManager()
                    .findById(id)
                    .orElseThrow();

            if (ra instanceof TemporaryAssignment a) {
                String date = s.nextLine();
                a.extend(date);
            }

        };

        command.execute(scanner, system);

        verify(assignment).extend("2030-01-01");

    }

}
package com.RBAC;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Scanner;

import static org.mockito.Mockito.*;

class UserCreateCommandTest {

    private RBACSystem system;
    private UserManager userManager;

    @BeforeEach
    void setUp() {

        system = mock(RBACSystem.class);
        userManager = mock(UserManager.class);

        when(system.getUserManager()).thenReturn(userManager);

    }

    @Test
    void userCreateCommandAddsUser() {

        Command command = (scanner, sys) -> {

            String username = scanner.nextLine();
            String fullName = scanner.nextLine();
            String email = scanner.nextLine();

            User user = new User(username, fullName, email);

            sys.getUserManager().add(user);
        };

        Scanner scanner = new Scanner("""
                john
                John Doe
                john@mail.com
                """);

        command.execute(scanner, system);

        verify(userManager).add(any(User.class));

    }

}
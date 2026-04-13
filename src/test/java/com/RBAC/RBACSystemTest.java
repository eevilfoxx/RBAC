package com.RBAC;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RBACSystemTest {

    @Test
    void initializeCreatesAdminUser() {

        RBACSystem system = new RBACSystem();


        UserManager userManager = system.getUserManager();
        RoleManager roleManager = system.getRoleManager();
        AssignmentManager assignmentManager = system.getAssignmentManager();

        User admin = new User("admin", "Admin User", "admin@mail.com");
        userManager.add(admin);

        Role adminRole = new Role("admin", "admin");
        Role managerRole = new Role("manager", "manager");
        Role viewerRole = new Role("viewer", "viewer");

        roleManager.add(adminRole);
        roleManager.add(managerRole);
        roleManager.add(viewerRole);

        assignmentManager.add(
                new PermanentAssignment(
                        admin,
                        adminRole,
                        AssignmentMetadata.now("test", "init")
                )
        );

        assertEquals(1, userManager.count());
        assertEquals(3, roleManager.count());
        assertEquals(1, assignmentManager.count());
    }

    @Test
    void statisticsGenerated() {

        RBACSystem system = new RBACSystem();

        UserManager userManager = system.getUserManager();
        RoleManager roleManager = system.getRoleManager();
        AssignmentManager assignmentManager = system.getAssignmentManager();

        userManager.add(new User("admin", "Admin", "admin@mail.com"));

        roleManager.add(new Role("manager", "manager"));
        roleManager.add(new Role("viewer", "viewer"));

        String stats = system.generateStatistics();

        assertTrue(stats.contains("Users"));
        assertTrue(stats.contains("Roles"));
        assertTrue(stats.contains("Assignments"));
    }
}
package com.RBAC;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RBACStressTest {

    private RBACSystem system;

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
        system.initialize();
    }

    @Test
    void initializeCreatesAdminUser() {
        UserManager userManager = system.getUserManager();
        RoleManager roleManager = system.getRoleManager();

        assertEquals(1, userManager.count());

        assertNotNull(roleManager.findByName("Admin"));
        assertNotNull(roleManager.findByName("Manager"));
        assertNotNull(roleManager.findByName("Viewer"));
    }

    @Test
    void adminUserHasAssignments() {
        UserManager userManager = system.getUserManager();
        AssignmentManager assignmentManager = system.getAssignmentManager();

        User admin = userManager.findAll().stream()
                .filter(u -> u.username().equals("admin"))
                .findFirst()
                .orElseThrow();

        assertEquals(1, assignmentManager.findByUser(admin).size());
    }

    @Test
    void systemStatisticsNotEmpty() {
        String stats = system.generateStatistics();

        assertNotNull(stats);
        assertTrue(stats.contains("Users"));
        assertTrue(stats.contains("Roles"));
        assertTrue(stats.contains("Assignments"));
    }
}
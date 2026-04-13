package com.RBAC;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RBACSystemTest {

    @Test
    void initializeCreatesAdminUser() {

        RBACSystem system = new RBACSystem();

        system.initialize();

        assertTrue(system.getUserManager().count() >= 1);

        assertNotNull(system.getRoleManager().findByName("Admin"));
        assertNotNull(system.getRoleManager().findByName("Manager"));
        assertNotNull(system.getRoleManager().findByName("Viewer"));

        assertTrue(system.getAssignmentManager().count() >= 1);
    }

    @Test
    void statisticsGenerated() {

        RBACSystem system = new RBACSystem();
        system.initialize();

        String stats = system.generateStatistics();

        assertNotNull(stats);
        assertTrue(stats.contains("Users"));
        assertTrue(stats.contains("Roles"));
        assertTrue(stats.contains("Assignments"));
        assertTrue(stats.contains("Audit entries"));
    }
}
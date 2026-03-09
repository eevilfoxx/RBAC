package com.RBAC;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RBACSystemTest {

    @Test
    void initializeCreatesAdminUser() {

        RBACSystem system = new RBACSystem();

        system.initialize();

        assertEquals(1, system.getUserManager().count());
        assertEquals(3, system.getRoleManager().count());
        assertEquals(1, system.getAssignmentManager().count());

    }

    @Test
    void statisticsGenerated() {

        RBACSystem system = new RBACSystem();

        String stats = system.generateStatistics();

        assertTrue(stats.contains("Users"));
        assertTrue(stats.contains("Roles"));
        assertTrue(stats.contains("Assignments"));

    }

}
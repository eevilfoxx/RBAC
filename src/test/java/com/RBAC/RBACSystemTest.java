package com.RBAC;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RBACSystemTest {

    private RBACSystem system;

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
        system.reset();
    }

    @Test
    void initializeCreatesAdminUser() {

        system.initialize();

        assertTrue(system.getUserManager().count() >= 1);

        assertNotNull(system.getRoleManager().findByName("Admin"));
        assertNotNull(system.getRoleManager().findByName("Manager"));
        assertNotNull(system.getRoleManager().findByName("Viewer"));

        assertTrue(system.getAssignmentManager().count() >= 1);
    }

    @Test
    void statisticsGenerated() {

        system.initialize();

        String stats = system.generateStatistics();

        assertTrue(stats.contains("Users"));
        assertTrue(stats.contains("Roles"));
        assertTrue(stats.contains("Assignments"));
    }
}
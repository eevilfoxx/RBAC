package com.RBAC;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportGeneratorTest {

    @Mock
    private UserManager userManager;

    @Mock
    private RoleManager roleManager;

    @Mock
    private AssignmentManager assignmentManager;

    private ReportGenerator reportGenerator;

    @BeforeEach
    void setUp() {
        reportGenerator = new ReportGenerator();
    }

    @Test
    void testGenerateUserReport() {
        User user1 = new User("john", "John Doe", "john@test.com");
        User user2 = new User("jane", "Jane Doe", "jane@test.com");

        Role role1 = new Role("admin", "Administrator");
        Role role2 = new Role("user", "Regular User");

        AssignmentMetadata meta1 = new AssignmentMetadata("admin", "2024-01-01", "Initial");
        AssignmentMetadata meta2 = new AssignmentMetadata("admin", "2024-01-02", "Initial");

        RoleAssignment assignment1 = new TemporaryAssignment(user1, role1, meta1);
        RoleAssignment assignment2 = new TemporaryAssignment(user1, role2, meta1);
        RoleAssignment assignment3 = new TemporaryAssignment(user2, role2, meta2);

        when(userManager.findAll()).thenReturn(Arrays.asList(user1, user2));
        when(assignmentManager.findByUser(user1)).thenReturn(Arrays.asList(assignment1, assignment2));
        when(assignmentManager.findByUser(user2)).thenReturn(List.of(assignment3));
        when(assignmentManager.findAll()).thenReturn(Arrays.asList(assignment1, assignment2, assignment3));


        String report = reportGenerator.generateUserReport(userManager, assignmentManager);

        assertNotNull(report);
        assertTrue(report.contains("USER REPORT"));
        assertTrue(report.contains("john"));
        assertTrue(report.contains("jane"));
        assertTrue(report.contains("admin"));
        assertTrue(report.contains("user"));
        assertTrue(report.contains("Total users: 2"));
    }

    @Test
    void testGenerateUserReportWithNoUsers() {
        when(userManager.findAll()).thenReturn(Collections.emptyList());
        when(assignmentManager.findAll()).thenReturn(Collections.emptyList());

        String report = reportGenerator.generateUserReport(userManager, assignmentManager);

        assertNotNull(report);
        assertTrue(report.contains("Total users: 0"));
    }

    @Test
    void testGenerateRoleReport() {
        Role role1 = new Role("admin", "Administrator");
        Role role2 = new Role("manager", "Manager");

        role1.addPermission(new Permission("read", "users", "Can read users"));
        role1.addPermission(new Permission("write", "users", "Can write users"));
        role2.addPermission(new Permission("read", "reports", "Can read reports"));

        User user1 = new User("john", "John Doe", "john@test.com");
        User user2 = new User("jane", "Jane Doe", "jane@test.com");

        AssignmentMetadata meta = new AssignmentMetadata("admin", "2024-01-01", "Initial");

        when(roleManager.findAll()).thenReturn(Arrays.asList(role1, role2));

        RoleAssignment assignment1 = new TemporaryAssignment(user1, role1, meta);
        RoleAssignment assignment2 = new TemporaryAssignment(user2, role1, meta);
        RoleAssignment assignment3 = new TemporaryAssignment(user1, role2, meta);

        when(assignmentManager.findByRole(role1)).thenReturn(Arrays.asList(assignment1, assignment2));
        when(assignmentManager.findByRole(role2)).thenReturn(List.of(assignment3));

        String report = reportGenerator.generateRoleReport(roleManager, assignmentManager);

        assertNotNull(report);
        assertTrue(report.contains("ROLE REPORT"));
        assertTrue(report.contains("admin"));
        assertTrue(report.contains("manager"));
        assertTrue(report.contains("2 users")); // admin role has 2 users
        assertTrue(report.contains("1 users")); // manager role has 1 user
    }

    @Test
    void testGeneratePermissionMatrix() {
        User user1 = new User("john", "John Doe", "john@test.com");
        User user2 = new User("jane", "Jane Doe", "jane@test.com");

        Role role1 = new Role("admin", "Administrator");
        Role role2 = new Role("user", "Regular User");

        role1.addPermission(new Permission("read", "users", "Can read"));
        role1.addPermission(new Permission("write", "users", "Can write"));
        role2.addPermission(new Permission("read", "reports", "Can read"));

        AssignmentMetadata meta1 = new AssignmentMetadata("admin", "2024-01-01", "Initial");
        AssignmentMetadata meta2 = new AssignmentMetadata("admin", "2024-01-02", "Initial");

        RoleAssignment assignment1 = new TemporaryAssignment(user1, role1, meta1);
        RoleAssignment assignment2 = new TemporaryAssignment(user1, role2, meta1);
        RoleAssignment assignment3 = new TemporaryAssignment(user2, role2, meta2);

        when(userManager.findAll()).thenReturn(Arrays.asList(user1, user2));
        when(assignmentManager.findByUser(user1)).thenReturn(Arrays.asList(assignment1, assignment2));
        when(assignmentManager.findByUser(user2)).thenReturn(List.of(assignment3));

        String report = reportGenerator.generatePermissionMatrix(userManager, assignmentManager);

        assertNotNull(report);
        assertTrue(report.contains("PERMISSION MATRIX"));
        assertTrue(report.contains("john"));
        assertTrue(report.contains("jane"));
        assertTrue(report.contains("users"));
        assertTrue(report.contains("reports"));
    }
}
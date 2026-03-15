package com.RBAC;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

        String uniqueId = UUID.randomUUID().toString().substring(0, 4);
        Role role1 = new Role("admin_role_" + uniqueId, "Administrator");
        Role role2 = new Role("user_role_" + uniqueId, "Regular User");

        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial");
        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Initial");

        String futureDate = DateUtils.addDays(DateUtils.getCurrentDate(), 30);

        TemporaryAssignment assignment1 = new TemporaryAssignment(user1, role1, meta1);
        assignment1.setExpiresAt(futureDate);

        TemporaryAssignment assignment2 = new TemporaryAssignment(user1, role2, meta1);
        assignment2.setExpiresAt(futureDate);

        TemporaryAssignment assignment3 = new TemporaryAssignment(user2, role2, meta2);
        assignment3.setExpiresAt(futureDate);

        when(userManager.findAll()).thenReturn(Arrays.asList(user1, user2));
        when(assignmentManager.findByUser(user1)).thenReturn(Arrays.asList(assignment1, assignment2));
        when(assignmentManager.findByUser(user2)).thenReturn(List.of(assignment3));
        when(assignmentManager.findAll()).thenReturn(Arrays.asList(assignment1, assignment2, assignment3));

        String report = reportGenerator.generateUserReport(userManager, assignmentManager);

        assertNotNull(report);
        assertTrue(report.contains("USER REPORT"));
        assertTrue(report.contains("john"));
        assertTrue(report.contains("jane"));
        assertTrue(report.contains("admin_role_" + uniqueId));
        assertTrue(report.contains("user_role_" + uniqueId));
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
        String uniqueId = UUID.randomUUID().toString().substring(0, 4);
        Role role1 = new Role("admin_role_" + uniqueId, "Administrator");
        Role role2 = new Role("manager_role_" + uniqueId, "Manager");

        role1.addPermission(new Permission("read", "users", "Can read users"));
        role1.addPermission(new Permission("write", "users", "Can write users"));
        role2.addPermission(new Permission("read", "reports", "Can read reports"));

        User user1 = new User("john", "John Doe", "john@test.com");
        User user2 = new User("jane", "Jane Doe", "jane@test.com");

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Initial");

        when(roleManager.findAll()).thenReturn(Arrays.asList(role1, role2));

        String futureDate = DateUtils.addDays(DateUtils.getCurrentDate(), 30);

        TemporaryAssignment assignment1 = new TemporaryAssignment(user1, role1, meta);
        assignment1.setExpiresAt(futureDate);

        TemporaryAssignment assignment2 = new TemporaryAssignment(user2, role1, meta);
        assignment2.setExpiresAt(futureDate);

        TemporaryAssignment assignment3 = new TemporaryAssignment(user1, role2, meta);
        assignment3.setExpiresAt(futureDate);

        when(assignmentManager.findByRole(role1)).thenReturn(Arrays.asList(assignment1, assignment2));
        when(assignmentManager.findByRole(role2)).thenReturn(List.of(assignment3));

        String report = reportGenerator.generateRoleReport(roleManager, assignmentManager);

        assertNotNull(report);
        assertTrue(report.contains("ROLE REPORT"));
        assertTrue(report.contains("admin_role_" + uniqueId));
        assertTrue(report.contains("manager_role_" + uniqueId));
        assertTrue(report.contains("2"));
        assertTrue(report.contains("1"));
    }

    @Test
    void testGenerateRoleReportWithNoRoles() {
        when(roleManager.findAll()).thenReturn(Collections.emptyList());

        String report = reportGenerator.generateRoleReport(roleManager, assignmentManager);

        assertNotNull(report);
        assertTrue(report.contains("Total roles: 0"));
    }

    @Test
    void testGeneratePermissionMatrix() {
        User user1 = new User("john", "John Doe", "john@test.com");
        User user2 = new User("jane", "Jane Doe", "jane@test.com");

        String uniqueId = UUID.randomUUID().toString().substring(0, 4);
        Role role1 = new Role("admin_role_" + uniqueId, "Administrator");
        Role role2 = new Role("user_role_" + uniqueId, "Regular User");

        role1.addPermission(new Permission("read", "users", "Can read"));
        role1.addPermission(new Permission("write", "users", "Can write"));
        role2.addPermission(new Permission("read", "reports", "Can read"));

        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial");
        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Initial");

        String futureDate = DateUtils.addDays(DateUtils.getCurrentDate(), 30);

        TemporaryAssignment assignment1 = new TemporaryAssignment(user1, role1, meta1);
        assignment1.setExpiresAt(futureDate);

        TemporaryAssignment assignment2 = new TemporaryAssignment(user1, role2, meta1);
        assignment2.setExpiresAt(futureDate);

        TemporaryAssignment assignment3 = new TemporaryAssignment(user2, role2, meta2);
        assignment3.setExpiresAt(futureDate);

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

    @Test
    void testGeneratePermissionMatrixWithNoUsers() {
        when(userManager.findAll()).thenReturn(Collections.emptyList());

        String report = reportGenerator.generatePermissionMatrix(userManager, assignmentManager);

        assertNotNull(report);
        assertTrue(report.contains("PERMISSION MATRIX"));
    }

    @Test
    void testGeneratePermissionMatrixWithNoPermissions() {
        User user1 = new User("john", "John Doe", "john@test.com");

        String uniqueId = UUID.randomUUID().toString().substring(0, 4);
        Role role1 = new Role("empty_role_" + uniqueId, "Empty Role");

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Initial");

        String futureDate = DateUtils.addDays(DateUtils.getCurrentDate(), 30);

        TemporaryAssignment assignment = new TemporaryAssignment(user1, role1, meta);
        assignment.setExpiresAt(futureDate);

        when(userManager.findAll()).thenReturn(List.of(user1));
        when(assignmentManager.findByUser(user1)).thenReturn(List.of(assignment));

        String report = reportGenerator.generatePermissionMatrix(userManager, assignmentManager);

        assertNotNull(report);
        assertTrue(report.contains("PERMISSION MATRIX"));
        assertTrue(report.contains("john"));
    }
}
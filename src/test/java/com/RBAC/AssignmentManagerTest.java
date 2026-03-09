package com.RBAC;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentManagerTest {

    private AssignmentManager manager;

    @Mock
    private User user;

    @Mock
    private Role role;

    private AssignmentMetadata metadata;

    @BeforeEach
    void setUp() {
        manager = new AssignmentManager();
        metadata = new AssignmentMetadata("system", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), "Test assignment");
    }

    @Test
    void addAssignment() {
        TemporaryAssignment assignment = new TemporaryAssignment(user, role, metadata);
        assignment.expiresAt = LocalDate.now().plusDays(5).toString();

        manager.add(assignment);

        assertEquals(1, manager.count());
    }

    @Test
    void duplicateAssignmentShouldThrow() {
        TemporaryAssignment assignment1 = new TemporaryAssignment(user, role, metadata);
        assignment1.expiresAt = LocalDate.now().plusDays(5).toString();

        manager.add(assignment1);

        TemporaryAssignment assignment2 = new TemporaryAssignment(user, role, metadata);
        assignment2.expiresAt = LocalDate.now().plusDays(10).toString();

        assertThrows(IllegalStateException.class,
                () -> manager.add(assignment2),
                "Должно выбрасываться исключение при добавлении дублирующей роли пользователю");
    }

    @Test
    void revokeAssignment() {
        TemporaryAssignment assignment = new TemporaryAssignment(user, role, metadata);
        assignment.expiresAt = LocalDate.now().plusDays(5).toString();

        manager.add(assignment);

        assertTrue(assignment.isActive());

        manager.revokeAssignment(assignment.assignmentId());

        assertFalse(assignment.isActive(), "Assignment должен быть деактивирован после revoke");
    }

    @Test
    void extendAssignment() {
        TemporaryAssignment assignment = new TemporaryAssignment(user, role, metadata);
        assignment.expiresAt = LocalDate.now().plusDays(1).toString();

        manager.add(assignment);

        String newExpiration = "2030-01-01";
        manager.extendTemporaryAssignment(assignment.assignmentId(), newExpiration);

        assertEquals(newExpiration, assignment.expiresAt, "Дата окончания должна быть обновлена");
    }

    @Test
    void revokeNonExistingAssignment() {
        assertThrows(NoSuchElementException.class,
                () -> manager.revokeAssignment("999"));
    }
}
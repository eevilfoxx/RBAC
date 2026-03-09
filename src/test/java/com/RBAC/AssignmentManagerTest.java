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
    private RoleAssignment assignment;

    @Mock
    private RoleAssignment secondAssignment;

    @Mock
    private TemporaryAssignment temporaryAssignment;

    @Mock
    private User user;

    @Mock
    private Role role;

    @BeforeEach
    void setUp() {
        manager = new AssignmentManager();
    }

    @Test
    void addAssignment() {

        when(assignment.assignmentId()).thenReturn("1");
        when(assignment.user()).thenReturn(user);
        when(assignment.role()).thenReturn(role);
        when(assignment.isActive()).thenReturn(true);

        manager.add(assignment);

        assertEquals(1, manager.count());
    }

    @Test
    void duplicateAssignmentShouldThrow() {

        when(assignment.assignmentId()).thenReturn("1");
        when(assignment.user()).thenReturn(user);
        when(assignment.role()).thenReturn(role);
        when(assignment.isActive()).thenReturn(true);

        manager.add(assignment);

        when(secondAssignment.assignmentId()).thenReturn("2");
        when(secondAssignment.user()).thenReturn(user);
        when(secondAssignment.role()).thenReturn(role);
        when(secondAssignment.isActive()).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> manager.add(secondAssignment));
    }

    @Test
    void revokePermanentAssignment() {
        when(assignment.assignmentId()).thenReturn("1");
        when(assignment.user()).thenReturn(user);
        when(assignment.role()).thenReturn(role);
        when(assignment.isActive()).thenReturn(true);

        manager.add(assignment);

        manager.revokeAssignment("1");

        // Для постоянного назначения оно должно быть удалено из менеджера
        assertEquals(0, manager.count());
        assertTrue(manager.findById("1").isEmpty());
    }

    @Test
    void revokeTemporaryAssignment() {
        String assignmentId = "temp1";
        String pastDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        when(temporaryAssignment.assignmentId()).thenReturn(assignmentId);
        when(temporaryAssignment.user()).thenReturn(user);
        when(temporaryAssignment.role()).thenReturn(role);
        when(temporaryAssignment.isActive()).thenReturn(true);

        manager.add(temporaryAssignment);

        manager.revokeAssignment(assignmentId);

        // Для временного назначения вызывается extend с прошедшей датой
        verify(temporaryAssignment).extend(argThat(date -> {
            try {
                LocalDate parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return parsedDate.equals(LocalDate.now().minusDays(1));
            } catch (Exception e) {
                return false;
            }
        }));

        // Назначение должно остаться в менеджере (но стать неактивным)
        assertEquals(1, manager.count());
        assertTrue(manager.findById(assignmentId).isPresent());
    }

    @Test
    void extendTemporaryAssignment() {
        String assignmentId = "temp1";
        String newExpirationDate = "2030-01-01";

        when(temporaryAssignment.assignmentId()).thenReturn(assignmentId);
        when(temporaryAssignment.user()).thenReturn(user);
        when(temporaryAssignment.role()).thenReturn(role);

        manager.add(temporaryAssignment);

        manager.extendTemporaryAssignment(assignmentId, newExpirationDate);

        verify(temporaryAssignment).extend(newExpirationDate);
    }

    @Test
    void revokeNonExistingAssignment() {
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> manager.revokeAssignment("999"));

        assertEquals("Assignment not found", exception.getMessage());
    }

    @Test
    void extendNonExistingAssignment() {
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> manager.extendTemporaryAssignment("999", "2030-01-01"));

        assertEquals("Assignment not found", exception.getMessage());
    }
}
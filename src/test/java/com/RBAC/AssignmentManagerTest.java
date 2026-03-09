import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

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
    private User user;

    @Mock
    private Role role;

    @BeforeEach
    void setUp() {
        manager = new AssignmentManager();
    }

    @Test
    void addAssignment() {

        when(assignment.getId()).thenReturn("1");
        when(assignment.getUser()).thenReturn(user);
        when(assignment.getRole()).thenReturn(role);
        when(assignment.isActive()).thenReturn(true);

        manager.add(assignment);

        assertEquals(1, manager.count());
    }

    @Test
    void duplicateAssignmentShouldThrow() {

        when(assignment.getId()).thenReturn("1");
        when(assignment.getUser()).thenReturn(user);
        when(assignment.getRole()).thenReturn(role);
        when(assignment.isActive()).thenReturn(true);

        manager.add(assignment);

        when(secondAssignment.getId()).thenReturn("2");
        when(secondAssignment.getUser()).thenReturn(user);
        when(secondAssignment.getRole()).thenReturn(role);
        when(secondAssignment.isActive()).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> manager.add(secondAssignment));
    }

    @Test
    void revokeAssignment() {

        when(assignment.getId()).thenReturn("1");
        when(assignment.getUser()).thenReturn(user);
        when(assignment.getRole()).thenReturn(role);
        when(assignment.isActive()).thenReturn(true);

        manager.add(assignment);

        manager.revokeAssignment("1");

        verify(assignment).deactivate();
    }

    @Test
    void extendAssignment() {

        when(assignment.getId()).thenReturn("1");

        manager.add(assignment);

        manager.extendTemporaryAssignment("1", "2030-01-01");

        verify(assignment).extend(LocalDate.parse("2030-01-01"));
    }

    @Test
    void revokeNonExistingAssignment() {

        assertThrows(NoSuchElementException.class,
                () -> manager.revokeAssignment("999"));
    }
}
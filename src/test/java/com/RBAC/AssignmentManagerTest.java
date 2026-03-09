import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.*;

class AssignmentManagerTest {

    private AssignmentManager manager;
    private User user;
    private Role role;
    private Permission perm;

    @BeforeEach
    void setUp() {
        manager = new AssignmentManager();
        user = new User("john", "John", "john@mail.com");
        role = new Role("1", "ADMIN");
        perm = new Permission("READ", "USER");
        role.getPermissions().add(perm);
    }

    @Test
    void addAssignment_success() {
        RoleAssignment a = new RoleAssignment(
                "A1", user, role, "PERMANENT",
                LocalDate.now(), null
        );

        manager.add(a);
        assertEquals(1, manager.count());
    }

    @Test
    void addDuplicateActiveAssignment_throwsException() {
        RoleAssignment a1 = new RoleAssignment(
                "A1", user, role, "PERMANENT",
                LocalDate.now(), null
        );
        RoleAssignment a2 = new RoleAssignment(
                "A2", user, role, "PERMANENT",
                LocalDate.now(), null
        );

        manager.add(a1);
        assertThrows(IllegalStateException.class, () -> manager.add(a2));
    }

    @Test
    void userHasRole_true() {
        manager.add(new RoleAssignment(
                "A1", user, role, "PERMANENT",
                LocalDate.now(), null
        ));

        assertTrue(manager.userHasRole(user, role));
    }

    @Test
    void userHasPermission_true() {
        manager.add(new RoleAssignment(
                "A1", user, role, "PERMANENT",
                LocalDate.now(), null
        ));

        assertTrue(manager.userHasPermission(user, "READ", "USER"));
    }

    @Test
    void revokeAssignment_deactivates() {
        RoleAssignment a = new RoleAssignment(
                "A1", user, role, "PERMANENT",
                LocalDate.now(), null
        );
        manager.add(a);
        manager.revokeAssignment("A1");

        assertFalse(manager.getActiveAssignments().contains(a));
    }

    @Test
    void extendTemporaryAssignment_updatesDate() {
        RoleAssignment a = new RoleAssignment(
                "A1", user, role, "TEMPORARY",
                LocalDate.now(), LocalDate.now().plusDays(5)
        );
        manager.add(a);

        manager.extendTemporaryAssignment("A1", "2030-01-01");
        assertEquals(LocalDate.of(2030,1,1), a.getExpiresAt());
    }

    @Test
    void findByFilter_and_sort() {
        RoleAssignment a1 = new RoleAssignment(
                "1", user, role, "PERMANENT",
                LocalDate.of(2023,1,1), null
        );
        RoleAssignment a2 = new RoleAssignment(
                "2", user, role, "PERMANENT",
                LocalDate.of(2024,1,1), null
        );

        manager.add(a1);
        manager.revokeAssignment("1");
        manager.add(a2);

        List<RoleAssignment> result = manager.findAll(
                AssignmentFilters.activeOnly(),
                Comparator.comparing(RoleAssignment::getAssignedAt)
        );

        assertEquals(1, result.size());
        assertEquals("2", result.get(0).getId());
    }
}
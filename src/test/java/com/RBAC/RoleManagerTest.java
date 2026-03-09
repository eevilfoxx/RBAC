import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class RoleManagerTest {

    private RoleManager manager;
    private Role admin;
    private Permission read;
    private Permission write;

    @BeforeEach
    void setUp() {
        manager = new RoleManager();
        admin = new Role("1", "ADMIN");
        read = new Permission("READ", "USER");
        write = new Permission("WRITE", "USER");

        manager.add(admin);
    }

    @Test
    void addRole_duplicateName_throwsException() {
        Role r = new Role("2", "ADMIN");
        assertThrows(IllegalArgumentException.class, () -> manager.add(r));
    }

    @Test
    void addPermissionToRole_success() {
        manager.addPermissionToRole("ADMIN", read);
        assertTrue(admin.getPermissions().contains(read));
    }

    @Test
    void removePermissionFromRole_success() {
        admin.getPermissions().add(write);
        manager.removePermissionFromRole("ADMIN", write);

        assertFalse(admin.getPermissions().contains(write));
    }

    @Test
    void findRolesWithPermission() {
        admin.getPermissions().add(read);

        List<Role> roles = manager.findRolesWithPermission("READ", "USER");
        assertEquals(1, roles.size());
        assertEquals("ADMIN", roles.get(0).getName());
    }

    @Test
    void findByFilter_and_sort() {
        Role user = new Role("2", "USER");
        manager.add(user);

        List<Role> result = manager.findAll(
                RoleFilters.byNameContains(""),
                Comparator.comparing(Role::getName)
        );

        assertEquals(List.of("ADMIN", "USER"),
                result.stream().map(Role::getName).toList());
    }
}
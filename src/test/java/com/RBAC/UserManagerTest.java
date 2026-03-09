import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {

    private UserManager manager;

    @BeforeEach
    void setUp() {
        manager = new UserManager();
    }

    @Test
    void addUser_success() {
        User u = new User("john", "John Doe", "john@mail.com");
        manager.add(u);

        assertEquals(1, manager.count());
        assertTrue(manager.exists("john"));
    }

    @Test
    void addUser_duplicateUsername_throwsException() {
        User u1 = new User("john", "John Doe", "john@mail.com");
        User u2 = new User("john", "Johnny", "johnny@mail.com");

        manager.add(u1);
        assertThrows(IllegalArgumentException.class, () -> manager.add(u2));
    }

    @Test
    void findByUsername_found() {
        User u = new User("anna", "Anna Smith", "anna@mail.com");
        manager.add(u);

        Optional<User> result = manager.findByUsername("anna");
        assertTrue(result.isPresent());
        assertEquals("Anna Smith", result.get().getFullName());
    }

    @Test
    void updateUser_success() {
        User u = new User("kate", "Kate", "k@mail.com");
        manager.add(u);

        manager.update("kate", "Kate Updated", "new@mail.com");

        User updated = manager.findByUsername("kate").orElseThrow();
        assertEquals("Kate Updated", updated.getFullName());
        assertEquals("new@mail.com", updated.getEmail());
    }

    @Test
    void updateNonExistingUser_throwsException() {
        assertThrows(NoSuchElementException.class,
                () -> manager.update("ghost", "X", "x@mail.com"));
    }

    @Test
    void findByFilter_and_sorter() {
        manager.add(new User("b", "Bob", "b@mail.com"));
        manager.add(new User("a", "Alice", "a@mail.com"));

        List<User> result = manager.findAll(
                UserFilters.byUsernameContains(""),
                Comparator.comparing(User::getUsername)
        );

        assertEquals(List.of("a", "b"),
                result.stream().map(User::getUsername).toList());
    }
}
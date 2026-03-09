import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagerTest {

    private UserManager manager;

    @Mock
    private User user;

    @BeforeEach
    void setUp() {
        manager = new UserManager();
    }

    @Test
    void addUser() {

        when(user.getUsername()).thenReturn("john");
        when(user.getEmail()).thenReturn("john@mail.com");

        manager.add(user);

        assertEquals(1, manager.count());
    }

    @Test
    void duplicateUserShouldThrow() {

        when(user.getUsername()).thenReturn("john");
        when(user.getEmail()).thenReturn("john@mail.com");

        manager.add(user);

        assertThrows(IllegalArgumentException.class,
                () -> manager.add(user));
    }

    @Test
    void removeUser() {

        when(user.getUsername()).thenReturn("john");
        when(user.getEmail()).thenReturn("john@mail.com");

        manager.add(user);

        assertTrue(manager.remove(user));
    }
}
package pl.dolien.echohub.user;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private static final String USER_NOT_FOUND = "User with id %s not found.";

    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        initUser();
    }

    @Test
    void shouldReturnUserByPublicId() {
        when(userRepository.findByPublicId(user.getId())).thenReturn(of(user));

        User foundUser = userService.findUserByPublicId(user.getId());

        assertEquals(user, foundUser);
        verify(userRepository, times(1)).findByPublicId(user.getId());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByPublicId(user.getId())).thenReturn(Optional.empty());

        var exception = assertThrows(EntityNotFoundException.class, () -> userService.findUserByPublicId(user.getId()));
        assertEquals(String.format(USER_NOT_FOUND, user.getId()), exception.getMessage());
    }

    private void initUser() {
        user = User.builder()
                .id("1")
                .build();
    }
}
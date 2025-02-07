package pl.dolien.echohub.user;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import pl.dolien.echohub.user.dto.UserResponse;

import java.util.List;
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
    @Mock
    private Authentication auth;
    private User user;
    private UserResponse userResponse;

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

    @Test
    void shouldReturnAllUsersExceptSelf() {
        mockGetAllUsersExceptSelf();

        List<UserResponse> users = userService.getAllUsersExceptSelf(auth);

        assertGetAllUsersExceptSelf(users);
        verifyGetAllUsersExceptSelf();
    }

    @Test
    void shouldSaveUser() {
        userService.saveUser(user);

        verify(userRepository, times(1)).save(user);
    }

    private void mockGetAllUsersExceptSelf() {
        when(auth.getName()).thenReturn(user.getId());
        when(userRepository.findAllUsersExceptSelf(user.getId())).thenReturn(List.of(user));
    }

    private void verifyGetAllUsersExceptSelf() {
        verify(auth, times(1)).getName();
        verify(userRepository, times(1)).findAllUsersExceptSelf(user.getId());
    }

    private void assertGetAllUsersExceptSelf(List<UserResponse> users) {
        assertEquals(List.of(userResponse).size(), users.size());
        assertEquals(userResponse.getId(), users.get(0).getId());
    }

    private void initUser() {
        user = User.builder()
                .id("1")
                .build();

        userResponse = UserResponse.builder()
                .id(user.getId())
                .build();
    }
}
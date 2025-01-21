package pl.dolien.echohub.user;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pl.dolien.echohub.user.dto.UserResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String USER_NOT_FOUND = "User with id %s not found.";
    private final UserRepository userRepository;

    public User findUserByPublicId(String userId) {
        return userRepository.findByPublicId(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(USER_NOT_FOUND, userId)));
    }

    public List<UserResponse> getAllUsersExceptSelf(Authentication connectedUser) {
        return userRepository.findAllUsersExceptSelf(connectedUser.getName())
                .stream()
                .map(UserMapper::toUserResponse)
                .toList();
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }
}

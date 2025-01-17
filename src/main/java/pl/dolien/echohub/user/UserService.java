package pl.dolien.echohub.user;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String USER_NOT_FOUND = "User with id %s not found.";
    private final UserRepository userRepository;

    public User findUserByPublicId(String userId) {
        return userRepository.findByPublicId(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(USER_NOT_FOUND, userId)));
    }
}

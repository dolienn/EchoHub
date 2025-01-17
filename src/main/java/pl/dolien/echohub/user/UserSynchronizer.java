package pl.dolien.echohub.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSynchronizer {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public void synchronizeWithIdp(Jwt token) {
        log.info("Synchronizing user with IDP");

        getUserEmail(token).ifPresentOrElse(
                userEmail -> {
                    log.info("Synchronizing user with email: {}", userEmail);
                    User user = userMapper.fromTokenAttributes(token.getClaims());
                    userRepository.save(user);
                },
                () -> log.warn("User synchronization skipped: Email not found in token")
        );
    }


    private Optional<String> getUserEmail(Jwt token) {
        return Optional.ofNullable(token.getClaims().get("email"))
                .map(Object::toString);
    }
}
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

    private static final String SYNCHRONIZE_WITH_IDP_MSG = "Synchronizing user with IDP";
    private static final String SYNCHRONIZE_USER_WITH_EMAIL_MSG = "Synchronizing user with email: {}";
    private static final String EMAIL_NOT_FOUND_IN_TOKEN_MSG = "User synchronization skipped: Email not found in token";
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public void synchronizeWithIdp(Jwt token) {
        log.info(SYNCHRONIZE_WITH_IDP_MSG);

        getUserEmail(token).ifPresentOrElse(
                userEmail -> {
                    log.info(SYNCHRONIZE_USER_WITH_EMAIL_MSG, userEmail);
                    User user = userMapper.fromTokenAttributes(token.getClaims());
                    userRepository.save(user);
                },
                () -> log.warn(EMAIL_NOT_FOUND_IN_TOKEN_MSG)
        );
    }


    private Optional<String> getUserEmail(Jwt token) {
        return Optional.ofNullable(token.getClaims().get("email"))
                .map(Object::toString);
    }
}
package pl.dolien.echohub.user;

import pl.dolien.echohub.user.dto.UserResponse;

import java.time.LocalDateTime;
import java.util.Map;

public class UserMapper {

    private UserMapper() {}

    public static User fromTokenAttributes(Map<String, Object> attributes) {
        User user = new User();

        user.setId(getAttributeAsString(attributes, "sub"));
        user.setFirstName(resolveFirstName(attributes));
        user.setLastName(getAttributeAsString(attributes, "family_name"));
        user.setEmail(getAttributeAsString(attributes, "email"));
        user.setLastSeen(LocalDateTime.now());

        return user;
    }

    private static String getAttributeAsString(Map<String, Object> attributes, String key) {
        return attributes.containsKey(key) ? attributes.get(key).toString() : null;
    }

    private static String resolveFirstName(Map<String, Object> attributes) {
        if (attributes.containsKey("given_name")) {
            return attributes.get("given_name").toString();
        } else if (attributes.containsKey("nickname")) {
            return attributes.get("nickname").toString();
        }
        return null;
    }

    public static UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .lastSeen(user.getLastSeen())
                .isOnline(user.isUserOnline())
                .build();
    }
}

package pl.dolien.echohub.user;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UserMapper {

    public User fromTokenAttributes(Map<String, Object> attributes) {
        User user = new User();

        user.setId(getAttributeAsString(attributes, "sub"));
        user.setFirstName(resolveFirstName(attributes));
        user.setLastName(getAttributeAsString(attributes, "family_name"));
        user.setEmail(getAttributeAsString(attributes, "email"));
        user.setLastSeen(LocalDateTime.now());

        return user;
    }

    private String getAttributeAsString(Map<String, Object> attributes, String key) {
        return attributes.containsKey(key) ? attributes.get(key).toString() : null;
    }

    private String resolveFirstName(Map<String, Object> attributes) {
        if (attributes.containsKey("given_name")) {
            return attributes.get("given_name").toString();
        } else if (attributes.containsKey("nickname")) {
            return attributes.get("nickname").toString();
        }
        return null;
    }
}

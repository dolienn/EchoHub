package pl.dolien.echohub.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import static pl.dolien.echohub.user.UserConstants.FIND_USER_BY_EMAIL;
import static pl.dolien.echohub.user.UserConstants.FIND_USER_BY_PUBLIC_ID;

public interface UserRepository extends JpaRepository<User, String> {
    @Query(name = FIND_USER_BY_EMAIL)
    Optional<User> findByEmail(@Param("email") String userEmail);

    @Query(name = FIND_USER_BY_PUBLIC_ID)
    Optional<User> findByPublicId(String publicId);
}

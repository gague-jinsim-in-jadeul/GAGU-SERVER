package org.gagu.gagubackend.user.repository;

import org.gagu.gagubackend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
    User findByEmailAndLoginType(String name, String loginType);
    User findByEmail(String email);
    boolean existsByEmailAndLoginType(String email, String loginType);
}

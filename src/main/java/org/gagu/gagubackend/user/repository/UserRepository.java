package org.gagu.gagubackend.user.repository;

import org.gagu.gagubackend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
    User findByEmailAndLoginType(String name, String loginType);
    User findByEmailAndNickName(String email, String nickName);
    User findByEmail(String email);
    User findByNickName(String nickname);
    boolean existsByNickName(String nickname);
    boolean existsByEmailAndLoginType(String email, String loginType);
    boolean existsByEmailAndNickName(String email, String nickname);
}

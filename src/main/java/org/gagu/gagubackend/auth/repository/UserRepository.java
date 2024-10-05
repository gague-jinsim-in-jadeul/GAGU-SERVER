package org.gagu.gagubackend.auth.repository;

import org.gagu.gagubackend.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User,Long> {
    User findByEmailAndLoginType(String name, String loginType);
    List<User> findAllByEmailAndLoginType(String email, String loginType);
    User findByEmailAndNickName(String email, String nickName);
    User findByNickName(String nickname);
    boolean existsByNickName(String nickname);
    boolean existsByNickNameAndLoginType(String nickname, String loginType);
    boolean existsByEmailAndLoginType(String email, String loginType);
    boolean existsByEmailAndNickName(String email, String nickname);
}

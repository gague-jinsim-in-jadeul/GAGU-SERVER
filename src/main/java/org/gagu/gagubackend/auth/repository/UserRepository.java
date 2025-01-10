package org.gagu.gagubackend.auth.repository;

import org.gagu.gagubackend.auth.domain.User;
import org.gagu.gagubackend.auth.repository.custom.UserRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User,Long>, UserRepositoryCustom {
    User findByEmailAndLoginType(String name, String loginType);
    User findByResourceIdAndLoginType(String resourceId, String loginType);
    User findByEmailAndNickName(String email, String nickName);
    User findByNickName(String nickname);
    boolean existsByNickName(String nickname);
    boolean existsByResourceIdAndLoginType(String resourceId, String loginType);
    boolean existsByEmailAndNickName(String email, String nickname);
}

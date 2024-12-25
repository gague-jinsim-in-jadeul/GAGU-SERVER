package org.gagu.gagubackend.auth.repository.custom;

import org.gagu.gagubackend.auth.domain.User;

import java.util.Optional;

public interface UserRepositoryCustom {
    Optional<User> findUserByNickname(String nickname);
}

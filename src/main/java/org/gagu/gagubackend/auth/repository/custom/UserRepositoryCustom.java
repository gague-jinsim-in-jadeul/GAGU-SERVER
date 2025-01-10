package org.gagu.gagubackend.auth.repository.custom;

import org.gagu.gagubackend.auth.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryCustom {
    /**
     * 닉네임으로 유저 조회
     * @param nickname
     * @return Optional
     */
    Optional<User> findUserByNickname(String nickname);

    /**
     * 공방 회원가입 여부 확인
     * @param nickname
     * @param loginType
     * @return Optional
     */
    Optional<User> checkWorkshopExist(String nickname, String loginType);

    /**
     * 공방 조회 시 중복 된 이메일 존재하여 List 반환
     * @param email
     * @param type
     * @return Optional
     */
    Optional<List<User>> findWorkshops(String email, String type);

    /**
     * 소셜 로그인 유저 조회
     * @param resourceId
     * @param loginType
     * @return user
     */
    Optional<User> checkSocialUserExist(String resourceId, String loginType);

    /**
     * 페이지에서 공방 id 값으로 조회
     * @param id
     * @return user
     */
    Optional<User> findWorkshopById(Long id);
}

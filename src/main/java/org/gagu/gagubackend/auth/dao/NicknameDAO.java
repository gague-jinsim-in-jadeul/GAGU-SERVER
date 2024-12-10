package org.gagu.gagubackend.auth.dao;

public interface NicknameDAO {
    /**
     * 중복 닉네임 없을 때까지 생성 반복
     * @return 닉네임 생성 ex)GAGU#1
     */
    String generateNickName();
}

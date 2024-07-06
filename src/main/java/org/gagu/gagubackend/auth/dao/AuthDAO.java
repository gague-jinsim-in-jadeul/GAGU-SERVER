package org.gagu.gagubackend.auth.dao;

import org.gagu.gagubackend.auth.dto.request.RequestSaveUserDto;
import org.springframework.http.ResponseEntity;

public interface AuthDAO {
    /**
     * 유저 DB 조회 후 token 생성
     * 닉네임 중복 여부 확인 후 저장
     * @param requestSaveUserDto
     * @return JWT
     */
    ResponseEntity<?> login(RequestSaveUserDto requestSaveUserDto);
}

package org.gagu.gagubackend.auth.dao;

import org.gagu.gagubackend.auth.dto.request.RequestGeneralSignDto;
import org.gagu.gagubackend.auth.dto.request.RequestGeneralSignUpDto;
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

    /**
     * 유저 DB 조회 후 패스워드 확인
     * 공방 관계자 로그인
     * @param requestGeneralSignDto
     * @param type
     * @return
     */
    ResponseEntity<?> generalLogin(RequestGeneralSignDto requestGeneralSignDto, String type);
    ResponseEntity<?> changeUserProfile(String nickname, String fileUrl);
}

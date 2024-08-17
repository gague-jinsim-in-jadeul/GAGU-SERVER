package org.gagu.gagubackend.auth.dao;

import org.gagu.gagubackend.auth.dto.request.RequestAddressDto;
import org.gagu.gagubackend.auth.dto.request.RequestGeneralSignDto;
import org.gagu.gagubackend.auth.dto.request.RequestSaveUserDto;
import org.springframework.http.ResponseEntity;

public interface AuthDAO {
    /**
     * 유저 DB 조회 후 token 생성  / 닉네임 중복 여부 확인 후 저장
     * 회원가입
     * @param requestSaveUserDto
     * @return JWT
     */
    ResponseEntity<?> generalLogin(RequestSaveUserDto requestSaveUserDto);

    /**
     * 유저 DB 조회 후 패스워드 확인
     * 공방 관계자 로그인
     * @param requestGeneralSignDto
     * @param type
     * @return
     */
    ResponseEntity<?> workshopLogin(RequestGeneralSignDto requestGeneralSignDto, String type);

    /**
     * 유저 프로필 사진을 변경
     * @param nickname
     * @param fileUrl
     * @return
     */
    ResponseEntity<?> changeUserProfile(String nickname, String fileUrl);

    /**
     * 사용자 프로필 정보를 반환
     * @param nickname
     * @return
     */
    ResponseEntity<?> checkUserProfile(String nickname);

    /**
     * 사용자 주소를 저장 후 상태 반환
     * @param requestAddressDto
     * @return status, body
     */
    ResponseEntity<?> saveUserAddress(RequestAddressDto requestAddressDto, String nickname);

    /**
     * refresh 토큰 삭제
     * redis storage {access token : logout} save
     * @param token
     * @return
     */
    ResponseEntity<?> deleteToken(String token);
}

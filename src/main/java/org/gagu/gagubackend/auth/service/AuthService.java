package org.gagu.gagubackend.auth.service;

import org.gagu.gagubackend.auth.dto.request.RequestGeneralSignDto;
import org.gagu.gagubackend.auth.dto.request.RequestGeneralSignUpDto;
import org.gagu.gagubackend.auth.dto.request.RequestOauthSignDto;
import org.gagu.gagubackend.auth.dto.request.RequestSaveUserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {
    /**
     * 소셜 로그인 api 사용 o
     * @param authorizeCode
     * @param type
     * @return
     */
    ResponseEntity<?> signIn(String authorizeCode, String type);
    /**
     * 소셜 로그인 api 사용 x
     * @param requestOauthSignDto
     * @param type
     * @return
     */
    ResponseEntity<?> normalSignIn(RequestOauthSignDto requestOauthSignDto, String type);

    /**
     * 공방 관계자 회원가입
     * @param requestGeneralSignUpDto
     * @param type
     * @return
     */
    ResponseEntity<?> generalSingUp(RequestGeneralSignUpDto requestGeneralSignUpDto, String type);

    /**
     * 공방 관계자 로그인
     * @param requestGeneralSignDto
     * @param type
     * @return
     */
    ResponseEntity<?> generalSignIn(RequestGeneralSignDto requestGeneralSignDto, String type);

    /**
     * 회원가입 이후 프로필 이미지 변경
     * @param file
     * @param nickname
     * @return
     */
    ResponseEntity<?> changeProfile(MultipartFile file, String nickname);

    /**
     * 사용자 프로필 내용을 반환
     * @param nickname
     * @return
     */
    ResponseEntity<?> getProfile(String nickname);
}

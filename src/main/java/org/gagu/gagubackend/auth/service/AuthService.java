package org.gagu.gagubackend.auth.service;

import org.gagu.gagubackend.auth.dto.request.*;
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
     * @return jwt
     */
    ResponseEntity<?> normalSignIn(RequestOauthSignDto requestOauthSignDto, String type);

    /**
     * 공방 관계자 회원가입
     * @param requestGeneralSignUpDto
     * @param type
     * @return jwt
     */
    ResponseEntity<?> generalSingUp(RequestGeneralSignUpDto requestGeneralSignUpDto, String type);

    /**
     * 공방 관계자 로그인
     * @param requestGeneralSignDto
     * @param type
     * @return jwt
     */
    ResponseEntity<?> generalSignIn(RequestGeneralSignDto requestGeneralSignDto, String type);

    /**
     * 회원가입 이후 프로필 이미지 변경
     * @param file
     * @param nickname
     * @return 상태반환
     */
    ResponseEntity<?> changeProfile(MultipartFile file, String nickname);

    /**
     * 사용자 프로필 내용을 반환
     * @param nickname
     * @return info
     */
    ResponseEntity<?> getProfile(String nickname);

    /**
     * 사용자 주소를 저장 후 상태 반환
     * @param requestAddressDto
     * @return status, body
     */
    ResponseEntity<?> saveAddress(RequestAddressDto requestAddressDto, String nickname);

    /**
     * 사용자 로그아웃
     * @param token
     * @return
     */
    ResponseEntity<?> logOut(String token);

    /**
     * 유저 닉네임, 이메일, 주소 변경
     * @param requestChangeUserInfoDto
     * @param nickname
     * @return
     */
    ResponseEntity<?> updateUserInfo(RequestChangeUserInfoDto requestChangeUserInfoDto, String nickname);

    /**
     * 공방 정보 반환
     * @param id
     * @return
     */
    ResponseEntity<?> getWorkShopDetails(Long id);
}

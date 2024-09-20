package org.gagu.gagubackend.global.domain.enums;

import lombok.Getter;
import org.springframework.http.ResponseEntity;

@Getter
public enum ResultCode {
    OK(200, "성공"),
    FAIL(400,"실패"),
    UNAUTHORIZED(403, "권한 없음"),
    PASSWORD_NOT_MATCH(403, "비밀번호 불일치"),
    DELETED_USER(401, "탈퇴 유저입니다. 관리자에게 문의해주세요."),
    EXPIRED_TOKEN(401, "토큰 유효 기간 만료"),
    DUPLICATE_NICKNAME(400, "이미 존재하는 닉네임입니다."),
    DUPLICATE_USER(400,"이미 계정이 존재합니다"),
    DUPLICATE_CHATROOM(400,"이미 채팅방이 존재합니다."),
    NOT_IN_STORAGE(404, "스토리지에 저장되어 있지 않습니다."),
    BAD_REQUEST(400, "요청값이 잘못 됐습니다."),
    TOKEN_IS_NULL(400, "토큰 값이 누락됐습니다."),
    NO_AUTHORIZE_NUMBER(400, "인증번호가 누락됐습니다."),
    DUPLICATE_PROFILE(400,"이미 같은 프로필입니다."),
    ALREADY_NICKNAME(400,"동일한 닉네임입니다. 다른 닉네임을 선택해주세요."),
    ALREADY_EMAIL(400,"동일한 이메일입니다. 다른 이메일을 선택해주세요."),
    ALREADY_ADDRESS(400,"동일한 주소입니다. 다른 주소를 선택해주세요."),
    NOT_FOUND_TOKEN(401,"토큰이 비어있습니다."),
    DUPLICATE_EMAIL(400, "이미 존재하는 이메일입니다."),
    NOT_FOUND_USER(401, "사용자를 찾을 수 없습니다."),
    NOT_FOUND_CHATROOM(401,"채팅방을 찾을 수 없습니다.");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResponseEntity<String> toResponseEntity(){
        return ResponseEntity.status(this.code).body(this.message);
    }
}

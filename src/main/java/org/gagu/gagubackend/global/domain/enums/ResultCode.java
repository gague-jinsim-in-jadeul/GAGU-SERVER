package org.gagu.gagubackend.global.domain.enums;

import lombok.Getter;

@Getter
public enum ResultCode {
    OK(200, "성공"),
    FAIL(400,"실패"),
    UNAUTHORIZED(403, "권한 없음"),
    PASSWORD_NOT_MATCH(403, "비밀번호 불일치"),
    DELETED_USER(401, "탈퇴 유저"),
    EXPIRED_TOKEN(401, "토큰 유효 기간 만료"),
    DUPLICATION_NICKNAME(400, "이미 존재하는 닉네임입니다."),
    DUPLICATE_PROFILE(400,"이미 같은 프로필입니다."),
    DUPLICATION_EMAIL(400, "이미 존재하는 이메일입니다."),
    NOT_FOUND_USER(401, "사용자를 찾을 수 없습니다."),
    NOT_FOUND_CHATROOM(401,"채팅방을 찾을 수 없습니다.");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ResultCode resolve(int code) {
        for (ResultCode result : values()) {
            if (result.getCode() == code) {
                return result;
            }
        }
        return null;
    }
}

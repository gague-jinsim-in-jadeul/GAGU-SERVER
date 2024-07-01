package org.gagu.gagubackend.auth.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RequestSaveUserDto {
    private String name;
    private String nickName;
    private String password;
    private String phoneNumber;
    private String email;
    private String profileUrl;
    private String loginType;
    private boolean useAble;
}

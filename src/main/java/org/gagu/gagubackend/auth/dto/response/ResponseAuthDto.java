package org.gagu.gagubackend.auth.dto.response;

import lombok.*;
import org.gagu.gagubackend.auth.domain.User;
import org.gagu.gagubackend.global.domain.CommonResponse;
import org.gagu.gagubackend.global.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ResponseAuthDto {

    private String accessToken;
    private String nickname;
    private String name;
    private String resourceId;
    private CommonResponse<?> status;

    public ResponseAuthDto(User user, String accessToken){
        this.accessToken = accessToken;
        this.nickname = user.getNickName();
        this.name = user.getName();
        this.resourceId = user.getResourceId();
        this.status = CommonResponse.success();
    }
}
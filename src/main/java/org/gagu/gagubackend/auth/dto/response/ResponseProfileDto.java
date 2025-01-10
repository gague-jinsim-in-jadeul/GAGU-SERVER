package org.gagu.gagubackend.auth.dto.response;

import lombok.*;
import org.gagu.gagubackend.auth.domain.User;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ResponseProfileDto {
    private String nickname;
    private String name;
    private String email;
    private String loginTypeLogo;
    private String profileUrl;
    private String address;

    public ResponseProfileDto(User user){
        this.profileUrl = user.getProfileUrl();
        this.name = user.getName();
        this.email = user.getEmail();
        this.address = user.getAddress();
        this.nickname = user.getNickName();
    }
}

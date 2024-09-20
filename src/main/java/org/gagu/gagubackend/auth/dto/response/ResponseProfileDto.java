package org.gagu.gagubackend.auth.dto.response;

import lombok.*;

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
}

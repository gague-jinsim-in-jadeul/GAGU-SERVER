package org.gagu.gagubackend.user.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RequestUserInfoDto {
    private String userEmail;
    private String userNickname;
}

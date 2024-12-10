package org.gagu.gagubackend.auth.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RequestOauthSignDto {
    private String name;
    private String profileUrl;
    private String email;
    private String FCMToken;
    private String resourceId;
}

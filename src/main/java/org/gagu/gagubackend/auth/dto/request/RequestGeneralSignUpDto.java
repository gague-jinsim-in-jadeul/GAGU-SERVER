package org.gagu.gagubackend.auth.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RequestGeneralSignUpDto {
    private String email;
    private String password;
    private String profileUrl;
    private String workShopName;
    private String profileMessage;
    private String FCMToken;
}

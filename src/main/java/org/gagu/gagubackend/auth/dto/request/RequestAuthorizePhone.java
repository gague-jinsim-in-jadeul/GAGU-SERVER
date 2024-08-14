package org.gagu.gagubackend.auth.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RequestAuthorizePhone {
    private String phoneNumber;
    private String authorizationNumber;
}

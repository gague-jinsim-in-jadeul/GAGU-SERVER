package org.gagu.gagubackend.auth.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RequestChangeUserInfoDto {
    private String email;
    private String address;
}

package org.gagu.gagubackend.auth.dto.response;

import lombok.*;
import org.gagu.gagubackend.global.domain.CommonResponse;

@Getter
@Setter
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
}
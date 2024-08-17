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
    private String name;
    private CommonResponse<?> status;
}
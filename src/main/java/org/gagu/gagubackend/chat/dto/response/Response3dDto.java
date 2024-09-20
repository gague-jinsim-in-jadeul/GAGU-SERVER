package org.gagu.gagubackend.chat.dto.response;

import lombok.*;
import org.springframework.core.io.ByteArrayResource;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Response3dDto {
    private String objUrl;
    private String mtlUrl;
    private String texture_1_Url;
    private String texture_2_Url;
}

package org.gagu.gagubackend.chat.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Response3DDto {
    private String objUrl;
    private String mtlUrl;
    private String texture_1_Url;
    private String texture_2_Url;
}

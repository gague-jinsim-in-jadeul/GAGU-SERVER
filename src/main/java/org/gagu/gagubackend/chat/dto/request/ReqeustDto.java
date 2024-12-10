package org.gagu.gagubackend.chat.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReqeustDto {
    private String nickname;
    private String body;
}

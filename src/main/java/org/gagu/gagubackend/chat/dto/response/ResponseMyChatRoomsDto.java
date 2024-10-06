package org.gagu.gagubackend.chat.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ResponseMyChatRoomsDto {
    private Long id;
    private String roomName;
    private String updateAt;
}

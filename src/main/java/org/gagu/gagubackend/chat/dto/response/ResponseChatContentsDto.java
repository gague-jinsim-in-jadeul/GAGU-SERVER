package org.gagu.gagubackend.chat.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ResponseChatContentsDto {
    private String sendTime;
    private String sender;
    private String message;
    private Long chatRoomId;
}

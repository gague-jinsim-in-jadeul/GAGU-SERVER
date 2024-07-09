package org.gagu.gagubackend.chat.dto.request;

import lombok.*;
import org.gagu.gagubackend.global.domain.enums.MessageType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RequestChatContentsDto {
    private MessageType type;
    private String contents;
    private String nickname;
}

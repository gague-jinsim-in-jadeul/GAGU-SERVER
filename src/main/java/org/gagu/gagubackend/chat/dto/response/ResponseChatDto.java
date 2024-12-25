package org.gagu.gagubackend.chat.dto.response;

import lombok.*;

@Getter
@AllArgsConstructor
@ToString
@Builder
public class ResponseChatDto {
   private String contents;
   private String nickName;
   private String time;
}

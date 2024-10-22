package org.gagu.gagubackend.chat.dto.response;

import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ResponseChatDto {
   private String contents;
   private String nickName;
   private String time;
}

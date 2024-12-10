package org.gagu.gagubackend.chat.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RequestCreateChatRoomDto {
    private String sellerNickname; // 공방 이름
}

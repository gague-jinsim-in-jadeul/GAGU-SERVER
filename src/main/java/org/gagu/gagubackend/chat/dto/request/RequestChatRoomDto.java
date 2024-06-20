package org.gagu.gagubackend.chat.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RequestChatRoomDto {
    private String buyerEmail;
    private String sellerEmail;
}

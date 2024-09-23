package org.gagu.gagubackend.chat.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Builder
public class EstimateChatContentsDto extends RequestChatContentsDto{
    private Long id;
    private String price;
    private String description;
}

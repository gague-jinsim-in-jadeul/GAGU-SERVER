package org.gagu.gagubackend.chat.dto.request;

import lombok.*;
import org.gagu.gagubackend.auth.domain.User;


/**
 * description : 모바일에서 전달받은 객체를 매핑하는 DTO
 * @author sonmingi
 * @fileName RequestFCMSendDto
 * @since : 9/26/24
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RequestFCMSendDto {
    private User senderNickname;
    private String body;
}

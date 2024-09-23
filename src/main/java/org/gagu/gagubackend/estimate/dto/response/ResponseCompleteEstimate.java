package org.gagu.gagubackend.estimate.dto.response;

import lombok.*;
import org.gagu.gagubackend.chat.dto.response.ResponseChatDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class ResponseCompleteEstimate extends ResponseChatDto {
    private Long id;
    private String furnitureName;
    private String furniture3DObj;
    private String furniture3DMtl;
    private String furniture3DTexture1;
    private String furniture3DTexture2;
    private String createdDate;
    private String price;
}

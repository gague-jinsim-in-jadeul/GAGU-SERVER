package org.gagu.gagubackend.estimate.dto.response;

import lombok.*;
import org.gagu.gagubackend.chat.dto.response.ResponseChatDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ResponseCompleteEstimate {
    private Long id;
    private String furnitureName;
    private String furniture2DUrl;
    private String furnitureGlbUrl;
    private String furnitureGltfUrl;
    private String furniture3DUrl;
    private String createdDate;
    private String price;
    private String description;
}

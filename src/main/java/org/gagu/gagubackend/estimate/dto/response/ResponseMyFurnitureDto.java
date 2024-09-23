package org.gagu.gagubackend.estimate.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ResponseMyFurnitureDto {
    private Long id;
    private String furnitureName;
    private String furniture2DUrl;
    private String furniture3DObj;
    private String furniture3DMtl;
    private String furniture3DTexture1;
    private String furniture3DTexture2;
    private String createdDate;
}

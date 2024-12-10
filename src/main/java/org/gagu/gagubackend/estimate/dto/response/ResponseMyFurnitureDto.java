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
    private String furnitureGlbUrl;
    private String furnitureGltfUrl;
    private String createdDate;
}

package org.gagu.gagubackend.estimate.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RequestSaveFurnitureDto {
    private String furnitureName;
    private String furniture2DUrl;
    private String furnitureGlbUrl;
    private String furnitureGltfUrl;
}

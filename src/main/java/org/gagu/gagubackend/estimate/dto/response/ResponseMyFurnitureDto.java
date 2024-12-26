package org.gagu.gagubackend.estimate.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.gagu.gagubackend.estimate.domain.Estimate;

@Getter
@ToString
@Builder
public class ResponseMyFurnitureDto {
    private Long id;
    private String furnitureName;
    private String furniture2DUrl;
    private String furnitureGlbUrl;
    private String furnitureGltfUrl;
    private String createdDate;

    public ResponseMyFurnitureDto(Long id, String furnitureName, String furniture2DUrl, String furnitureGlbUrl, String furnitureGltfUrl, String createdDate) {
        this.id = id;
        this.furnitureName = furnitureName;
        this.furniture2DUrl = furniture2DUrl;
        this.furnitureGlbUrl = furnitureGlbUrl;
        this.furnitureGltfUrl = furnitureGltfUrl;
        this.createdDate = createdDate;
    }

    public ResponseMyFurnitureDto() {
    }

    public ResponseMyFurnitureDto entityMapper(Estimate estimate) {
        return new ResponseMyFurnitureDto(estimate.getId(),
                estimate.getFurnitureName(),
                estimate.getFurniture2DUrl(),
                estimate.getFurnitureGlbUrl(),
                estimate.getFurnitureGltfUrl(),
                estimate.getCreatedDate());
    }
}

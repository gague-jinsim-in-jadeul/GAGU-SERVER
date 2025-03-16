package org.gagu.gagubackend.estimate.dto.response;

import lombok.*;
import org.gagu.gagubackend.chat.dto.response.ResponseChatDto;
import org.gagu.gagubackend.estimate.domain.Estimate;

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
    private String furnitureUsdzUrl;
    private String modifiedDate;
    private String makerName;
    private String price;
    private String description;

    public ResponseCompleteEstimate(Estimate estimate){
        this.id = estimate.getId();
        this.furnitureName = estimate.getFurnitureName();
        this.furniture2DUrl = estimate.getFurniture2DUrl();
        this.furnitureGlbUrl = estimate.getFurnitureGlbUrl();
        this.furnitureGltfUrl = estimate.getFurnitureGltfUrl();
        this.furnitureUsdzUrl = estimate.getFurnitureUsdzfUrl();
        this.modifiedDate = estimate.getModifiedDate();
        this.makerName = estimate.getMakerName();
        this.price = estimate.getPrice();
        this.description = estimate.getDescription();
    }
}

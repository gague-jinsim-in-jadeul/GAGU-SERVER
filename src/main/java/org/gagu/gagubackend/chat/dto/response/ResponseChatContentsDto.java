package org.gagu.gagubackend.chat.dto.response;

import lombok.*;
import org.gagu.gagubackend.chat.domain.ChatContents;
import org.gagu.gagubackend.estimate.domain.Estimate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ResponseChatContentsDto {
    private String sendTime;
    private String sender;
    private String message;
    private EstimateInfo estimateInfo;
    private Long chatRoomId;

    public void generalBuilder(ChatContents contents){
        this.sendTime = contents.getSendTime();
        this.sender = contents.getSender().getNickName();
        this.message = contents.getMessage();
        this.chatRoomId = contents.getChatRoomId();
    }

    public void reqEstimateBuilder(ChatContents contents, Estimate estimate){
        generalBuilder(contents);
        this.estimateInfo = new EstimateInfo();
        this.estimateInfo.reqEstimateBuilder(estimate);
    }
    public void resEstimateBuilder(ChatContents contents, Estimate estimate){
        generalBuilder(contents);
        this.estimateInfo = new EstimateInfo();
        this.estimateInfo.repEstimateBuilder(estimate);
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class EstimateInfo{
        private Long estimateId;
        private String furnitureName;
        private String furniture2DUrl;
        private String furnitureGlbUrl;
        private String furnitureGltfUrl;
        private String createdDate;
        private String description;
        private String price;
        private String makerName;

        public void repEstimateBuilder(Estimate estimate){
            this.estimateId = estimate.getId();
            this.furnitureName = estimate.getFurnitureName();
            this.furniture2DUrl = estimate.getFurniture2DUrl();
            this.furnitureGlbUrl = estimate.getFurnitureGlbUrl();
            this.furnitureGltfUrl = estimate.getFurnitureGltfUrl();
            this.createdDate = estimate.getCreatedDate();
            this.price = estimate.getPrice();
            this.makerName = estimate.getMakerName();
            this.description = estimate.getDescription();
        }
        public void reqEstimateBuilder(Estimate estimate){
            this.estimateId = estimate.getId();
            this.furnitureName = estimate.getFurnitureName();
            this.furniture2DUrl = estimate.getFurniture2DUrl();
            this.furnitureGlbUrl = estimate.getFurnitureGlbUrl();
            this.furnitureGltfUrl = estimate.getFurnitureGltfUrl();
            this.createdDate = estimate.getCreatedDate();
        }
        public EstimateInfo(){}
    }
}

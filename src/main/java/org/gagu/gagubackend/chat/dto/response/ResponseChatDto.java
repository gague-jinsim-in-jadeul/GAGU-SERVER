package org.gagu.gagubackend.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.gagu.gagubackend.estimate.domain.Estimate;
import org.gagu.gagubackend.global.domain.enums.MessageType;

@Getter
@AllArgsConstructor
@ToString
@Builder
public class ResponseChatDto {
   private MessageType type;
   private ChatContentInfo chatContentInfo;
   private EstimateInfo estimateInfo;
   private String nickName;
   private String time;

   @Getter
   @Builder
   public static class ChatContentInfo {
      private String contents;

      public ChatContentInfo() {
      }

      public ChatContentInfo(String contents) {
         this.contents = contents;
      }
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

      public EstimateInfo(Estimate estimate){
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
      public EstimateInfo(){}
   }
}

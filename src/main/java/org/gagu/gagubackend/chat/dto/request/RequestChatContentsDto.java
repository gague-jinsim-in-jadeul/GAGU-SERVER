package org.gagu.gagubackend.chat.dto.request;

import lombok.*;
import org.gagu.gagubackend.global.domain.enums.MessageType;

@Getter
@ToString
@AllArgsConstructor
@Builder
public class RequestChatContentsDto {
    private MessageType type;
    private String prompt;
    private EstimateInfo estimateInfo;
    private ChatContentsInfo chatContentsInfo;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class EstimateInfo{
        private Long estimateId;
        private String furnitureName;
        private String furniture2DUrl;
        private String furnitureGlbUrl;
        private String furnitureGltfUrl;
        private String furnitureUsdzUrl;
        private String createdDate;
        public EstimateInfo(){}
    }
    @Getter
    @AllArgsConstructor
    @Builder
    public static class ChatContentsInfo{
        private String contents;
        public ChatContentsInfo(){}
    }
}

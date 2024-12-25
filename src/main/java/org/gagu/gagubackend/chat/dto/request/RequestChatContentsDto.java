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
        private String template;
        private Long estimateId;
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

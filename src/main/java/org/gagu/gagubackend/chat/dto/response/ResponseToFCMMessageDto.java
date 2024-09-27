package org.gagu.gagubackend.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * description : FCM 에 실제로 전송될 DTO
 * @author : sonmingi
 * @fileName : ResponseToFCMMessageDto
 * @since : 9/26/24
 */
@Getter
@Builder
public class ResponseToFCMMessageDto {
    private boolean validateOnly;
    private ResponseToFCMMessageDto.Message message;

    @Builder
    @AllArgsConstructor
    @Getter
    public static class Message{
        private ResponseToFCMMessageDto.Notification notification;
        private String token;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    public static class Notification{
        private String title;
        private String body;
        private String image;
    }
}

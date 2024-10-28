package org.gagu.gagubackend.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.chat.dto.request.RequestFCMSendDto;
import org.gagu.gagubackend.chat.service.ChatService;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.gagu.gagubackend.global.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fcm")
@Slf4j
@RequiredArgsConstructor
public class FCMController {
    private final ChatService chatService;
    @Operation(summary = "채팅 알림 전송 테스트", description = "푸쉬 알림 테스트 용 입니다.")
    @PostMapping("/notification")
    public ResponseEntity<?> pushMessage(@RequestBody RequestFCMSendDto requestFCMSendDto){
        log.info("[CHATTING-NOTIFICATION] send push message");

        return chatService.sendMessageTo(requestFCMSendDto);
    }
}

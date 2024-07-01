package org.gagu.gagubackend.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.chat.dto.request.RequestCreateChatRoomDto;
import org.gagu.gagubackend.chat.dto.request.RequestChatRoomDto;
import org.gagu.gagubackend.chat.service.ChatService;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.gagu.gagubackend.global.security.JwtTokenProvider;
import org.gagu.gagubackend.user.dto.request.RequestUserInfoDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@Slf4j
@RequiredArgsConstructor
public class ChatController {
    private final JwtTokenProvider jwtTokenProvider;
    private final ChatService chatService;
    @Operation(summary = "결제 이후 채팅방을 새로 생성합니다.", security = @SecurityRequirement(name="JWT"))
    @PostMapping("/new")
    public ResponseEntity<?> createChatRoom(
            HttpServletRequest request,
            @RequestBody RequestCreateChatRoomDto requestCreateChatRoomDto){
        String token = jwtTokenProvider.extractToken(request);
        log.info("[chat] token : {}", token);

        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(ResultCode.UNAUTHORIZED.getCode()).body("Invalid token");
        }

        Map<String, String> userInfoMap = jwtTokenProvider.getUserInfo(token);
        log.info("[chat] user info : {}", userInfoMap.toString());

        RequestUserInfoDto userInfoDto = RequestUserInfoDto.builder()
                .userEmail(userInfoMap.get("email")).userNickname(userInfoMap.get("nickname"))
                .build();

        return chatService.createChatRoom(userInfoDto, requestCreateChatRoomDto);
    }
    @Operation(summary = "채팅방을 나갑니다.", security = @SecurityRequirement(name = "JWT"))
    @DeleteMapping("/out/{roomId}")
    public ResponseEntity<?> removeChatRoom(
            HttpServletRequest request,
            @PathVariable Long roomId){

        return chatService.exitChatRoom(roomId);
    }
    @Operation(summary = "채팅방 내역을 조회합니다.", security = @SecurityRequirement(name = "JWT"))
    @GetMapping("/contents")
    public ResponseEntity<?> getChatRoom(
            @RequestHeader("Authorization") String authorization,
            @RequestBody RequestChatRoomDto requestChatRoomDto){

        return null;
    }
}

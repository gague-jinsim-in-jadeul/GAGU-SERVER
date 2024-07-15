package org.gagu.gagubackend.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.chat.domain.ChatRoom;
import org.gagu.gagubackend.chat.dto.request.RequestChatContentsDto;
import org.gagu.gagubackend.chat.dto.request.RequestCreateChatRoomDto;
import org.gagu.gagubackend.chat.dto.response.ResponseChatDto;
import org.gagu.gagubackend.chat.repository.ChatRoomMemberRepository;
import org.gagu.gagubackend.chat.repository.ChatRoomRepository;
import org.gagu.gagubackend.chat.service.ChatService;
import org.gagu.gagubackend.global.domain.enums.MessageType;
import org.gagu.gagubackend.global.exception.NotMemberException;
import org.gagu.gagubackend.global.security.JwtTokenProvider;
import org.gagu.gagubackend.user.domain.User;
import org.gagu.gagubackend.user.dto.request.RequestUserInfoDto;
import org.gagu.gagubackend.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/chat")
@Slf4j
@RequiredArgsConstructor
public class ChatController {
    private final JwtTokenProvider jwtTokenProvider;
    private final ChatService chatService;
    private final SimpMessagingTemplate template;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Operation(summary = "결제 전 채팅방을 새로 생성합니다.", security = @SecurityRequirement(name="JWT"))
    @PostMapping("/new")
    public ResponseEntity<?> createChatRoom(
            HttpServletRequest request,
            @RequestBody RequestCreateChatRoomDto requestCreateChatRoomDto){
        String token = jwtTokenProvider.extractToken(request);

        Map<String, String> userInfoMap = jwtTokenProvider.getUserInfo(token);
        log.info("[chat] user info : {}", userInfoMap.toString());

        RequestUserInfoDto userInfoDto = RequestUserInfoDto.builder()
                .userEmail(userInfoMap.get("email")).userNickname(userInfoMap.get("nickname"))
                .build();

        return chatService.createChatRoom(userInfoDto, requestCreateChatRoomDto);
    }
    @Operation(summary = "채팅방을 나갑니다.", security = @SecurityRequirement(name = "JWT"))
    @DeleteMapping("/out/{roomNumber}")
    public ResponseEntity<?> removeChatRoom(
            HttpServletRequest request,
            @PathVariable Long roomNumber){

        return chatService.exitChatRoom(roomNumber);
    }
    @Operation(summary = "채팅방 내역을 조회합니다.", security = @SecurityRequirement(name = "JWT"))
    @GetMapping("/contents/{roomNumber}")
    public ResponseEntity<?> getChatRoom(
            HttpServletRequest request,
            @RequestParam int page,
            @PathVariable Long roomNumber){

        Pageable pageable = PageRequest.of(page,10, Sort.Direction.DESC,"sendTime");

        String token = jwtTokenProvider.extractToken(request);
        String nickName = jwtTokenProvider.getUserNickName(token);

        return ResponseEntity.ok(chatService.getChatContents(nickName,pageable,roomNumber));
    }
    @Operation(summary = "내가 속한 채팅방 조회", description = "가구 의뢰 시 생성된 채팅방을 조회 후 반환합니다.")
    @GetMapping("/my-rooms")
    public ResponseEntity<?> getMyChatRooms(HttpServletRequest request,
                                            @RequestParam int page){
        String token = jwtTokenProvider.extractToken(request);
        String nickName = jwtTokenProvider.getUserNickName(token);

        Pageable pageable = PageRequest.of(page, 10, Sort.Direction.DESC,"createdAt");

        return ResponseEntity.ok(chatService.getMyChatRooms(nickName, pageable));
    }

    @MessageMapping("/gagu-chat/{roomNumber}") // mapping ex)/pub/gagu/chat
    public void sendMessage(RequestChatContentsDto message,
                                         SimpMessageHeaderAccessor accessor,
                                         @DestinationVariable Long roomNumber) throws Exception {

        log.info("[chat] room id : {}",roomNumber);

        String nickname = (String) accessor.getSessionAttributes().get("senderNickname");
        log.info("[chat] check memeber....");
        if (nickname == null) {
            throw new IllegalArgumentException("세션에 닉네임이 없습니다.");
        }
        message.setNickname(nickname);

        if(message.getType() == MessageType.SUBSCRIBE){ // type : SUBSCRIBE
            User member = userRepository.findByNickName(message.getNickname());
            Optional<ChatRoom> chatRoomList = chatRoomRepository.findById(roomNumber);
            if(chatRoomList.isPresent() && checkMember(chatRoomList.get(),member)){
                accessor.getSessionAttributes().put("chatRoomId",roomNumber);
            }else{
                throw new NotMemberException("채팅방에 속해 있지 않습니다.");
            }
        }else{ // type : SEND
            Long sessionRoomId = (Long) accessor.getSessionAttributes().get("chatRoomId");
            if(sessionRoomId == null || !sessionRoomId.equals(roomNumber)){
                throw new NotMemberException("채팅방에 속해 있지 않습니다.");
            }
        }
        log.info("[chat] complete check member");
        Thread.sleep(1000); // 비동기적으로 메시지를 처리하기 위해서 1초 지연(옵션)

        ResponseChatDto responseChatDto = chatService.sendContents(message,roomNumber);
        template.convertAndSend("/sub/chatroom/"+roomNumber,responseChatDto); // 구독하고 있는 채팅방에 전송
    }
    private boolean checkMember(ChatRoom roomId, User member){
        return chatRoomMemberRepository.existsChatRoomMemberByRoomIdAndMember(roomId,member);
    }
}

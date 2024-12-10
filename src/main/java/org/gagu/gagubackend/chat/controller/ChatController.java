package org.gagu.gagubackend.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.chat.domain.ChatRoom;
import org.gagu.gagubackend.chat.domain.ChatRoomMember;
import org.gagu.gagubackend.chat.dto.request.RequestChatContentsDto;
import org.gagu.gagubackend.chat.dto.request.RequestCreateChatRoomDto;
import org.gagu.gagubackend.chat.dto.response.ResponseChatDto;
import org.gagu.gagubackend.chat.repository.ChatRoomMemberRepository;
import org.gagu.gagubackend.chat.repository.ChatRoomRepository;
import org.gagu.gagubackend.chat.service.ChatService;
import org.gagu.gagubackend.estimate.domain.Estimate;
import org.gagu.gagubackend.estimate.repository.EstimateRepository;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.gagu.gagubackend.global.exception.ChatRoomNotFoundException;
import org.gagu.gagubackend.global.exception.NotFoundUserException;
import org.gagu.gagubackend.global.security.JwtTokenProvider;
import org.gagu.gagubackend.auth.domain.User;
import org.gagu.gagubackend.auth.dto.request.RequestUserInfoDto;
import org.gagu.gagubackend.auth.repository.UserRepository;
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
import java.util.stream.Collectors;

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
    private final EstimateRepository estimateRepository;

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
        if(roomNumber == null){
            return ResultCode.BAD_REQUEST.toResponseEntity();
        }
        String token = jwtTokenProvider.extractToken(request);
        if(token == null){
            return ResultCode.TOKEN_IS_NULL.toResponseEntity();
        }
        String nickName = jwtTokenProvider.getUserNickName(token);
        if(nickName.isEmpty()){
            return ResultCode.NOT_FOUND_USER.toResponseEntity();
        }

        return chatService.exitChatRoom(roomNumber, nickName);
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

        Pageable pageable = PageRequest.of(page, 10, Sort.Direction.DESC,"createdDate");

        return ResponseEntity.ok(chatService.getMyChatRooms(nickName, pageable));
    }

    @MessageMapping("/gagu-chat/{roomNumber}") // mapping ex)/pub/gagu-chat/{roomNumber}
    public void sendMessage(RequestChatContentsDto message,
                                         SimpMessageHeaderAccessor accessor,
                                         @DestinationVariable Long roomNumber) throws Exception {

        log.info("[chat] room id : {}",roomNumber);



        String nickname = (String) accessor.getSessionAttributes().get("senderNickname");
        log.info("[chat] check memeber....");
        if (nickname == null) {
            throw new IllegalArgumentException("세션에 닉네임이 없습니다.");
        }

        Long sessionRoomId = (Long) accessor.getSessionAttributes().get("chatRoomId");

        if(sessionRoomId==null){ // 처음 메세지 보내는 경우
            log.info("[chat] chatting session is empty room id : {}, nickname : {}", roomNumber,nickname);
            User user = userRepository.findByNickName(nickname);
            if(user == null){
                throw new NotFoundUserException();
            }
            Optional<ChatRoom> foundChatRoomList = chatRoomRepository.findById(roomNumber);
            String workshop = (String) accessor.getSessionAttributes().get("workshop");

            if(workshop == null){
                log.info("[socket] first chatting with workshop: {}", workshop);
                ChatRoom chatRoom = foundChatRoomList.get();
                List<ChatRoomMember> list = chatRoomMemberRepository.findAllByRoomId(chatRoom);
                for(ChatRoomMember e : list){
                    if(e.getMember().getRoles().get(0).equals("ROLE_WORKSHOP")) {
                        log.info("[socket] workshop is : {}", e.getMember().getNickName());
                        List<Estimate> estimates = estimateRepository.findAllByNickName(user);
                        log.info("[socket] esitmate : {}", estimates.toString());
                        log.info("[socket] collect estimates success!");

                        for (Estimate tmp : estimates) {
                            if ((tmp.getPrice() == null) && (tmp.getDescription() == null)) {
                                tmp.setMakerName(e.getMember().getNickName());
                                estimateRepository.save(tmp);
                                log.info("[socket] update estimates success!");
                                accessor.getSessionAttributes().putIfAbsent("workshop", e.getMember().getNickName());
                                }
                            }
                       }
                    }
                }

            if(!foundChatRoomList.isEmpty()){
                ChatRoom chatRoom = foundChatRoomList.get();
                if(checkMember(chatRoom, user)){ // 채팅방 권한이 있다면
                    accessor.getSessionAttributes().putIfAbsent("chatRoomId", roomNumber);// 세션에 저장되어 있지 않을때, 세션에 저장
                    log.info("[chat] successfully put room id to session");
                }
                log.info("[chat] complete check member");
                Thread.sleep(1000); // 비동기적으로 메시지를 처리하기 위해서 1초 지연(옵션)
                log.info("[chat] message : {}", message.getContents());
                ResponseChatDto responseChatDto = chatService.sendContents(message,roomNumber,nickname);
                template.convertAndSend("/sub/chatroom/"+roomNumber,responseChatDto); // 구독하고 있는 채팅방에 전송
            }else{
                throw new ChatRoomNotFoundException();
            }
        }else{
            log.info("[chat] complete check member");
            Thread.sleep(1000); // 비동기적으로 메시지를 처리하기 위해서 1초 지연(옵션)
            log.info("[chat] question : {}", message.getContents());
            ResponseChatDto responseChatDto = chatService.sendContents(message,roomNumber,nickname);
            template.convertAndSend("/sub/chatroom/"+roomNumber,responseChatDto); // 구독하고 있는 채팅방에 전송
        }
    }

    private boolean checkMember(ChatRoom roomId, User member){
        return chatRoomMemberRepository.existsChatRoomMemberByRoomIdAndMember(roomId,member);
    }
}

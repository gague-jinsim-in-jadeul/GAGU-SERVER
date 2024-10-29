package org.gagu.gagubackend.chat.dao.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.chat.config.FCMConfig;
import org.gagu.gagubackend.chat.dao.ChatDAO;
import org.gagu.gagubackend.chat.domain.ChatContents;
import org.gagu.gagubackend.chat.domain.ChatRoom;
import org.gagu.gagubackend.chat.domain.ChatRoomMember;
import org.gagu.gagubackend.chat.dto.request.RequestChatContentsDto;
import org.gagu.gagubackend.chat.dto.request.RequestCreateChatRoomDto;
import org.gagu.gagubackend.chat.dto.request.RequestFCMSendDto;
import org.gagu.gagubackend.chat.dto.response.ResponseChatContentsDto;
import org.gagu.gagubackend.chat.dto.response.ResponseChatDto;
import org.gagu.gagubackend.chat.dto.response.ResponseMyChatRoomsDto;
import org.gagu.gagubackend.chat.repository.ChatContentsRepository;
import org.gagu.gagubackend.chat.repository.ChatRoomMemberRepository;
import org.gagu.gagubackend.chat.repository.ChatRoomRepository;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.gagu.gagubackend.global.exception.ChatRoomNotFoundException;
import org.gagu.gagubackend.global.exception.NotMemberException;
import org.gagu.gagubackend.auth.domain.User;
import org.gagu.gagubackend.auth.dto.request.RequestUserInfoDto;
import org.gagu.gagubackend.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatDAOImpl implements ChatDAO {
    private final UserRepository userRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatContentsRepository chatContentsRepository;
    private final FirebaseMessaging firebaseMessaging;

    @Override
    public ResponseEntity<?> createChatRoom(RequestUserInfoDto userInfoDto, RequestCreateChatRoomDto requestCreateChatRoomDto) {
        log.info("[chat] create chat room");
        String userEmail = userInfoDto.getUserEmail();
        String userNickname = userInfoDto.getUserNickname();
        String workShopName = requestCreateChatRoomDto.getSellerNickname();

        if (checkUserExist(userEmail, userNickname) && checkWorkshopExist(workShopName)) {
            User buyer = userRepository.findByEmailAndNickName(userEmail, userNickname);
            User workshop = userRepository.findByNickName(workShopName);
            log.info("[chat] user is exist!");

            log.info("[chat] is check chatroom exist....");
            if (areUsersInSameRoom(buyer, workshop)) {
                log.warn("[chat] chatroom is already exist!");
                Optional<ChatRoomMember> chatRoomMember = chatRoomMemberRepository.findChatRoomMemberByMembers(buyer, workshop);
                Long roomId = chatRoomMember.get().getRoomId().getId();
                return ResponseEntity.status(ResultCode.DUPLICATE_CHATROOM.getCode()).body(roomId);
            }

            String chatRoomName = createChatRoomName(buyer.getName(), workShopName);

            ChatRoom newChatRoom = new ChatRoom();
            newChatRoom.setRoomName(chatRoomName);

            chatRoomRepository.save(newChatRoom);

            ChatRoomMember buyerMember = ChatRoomMember.builder()
                    .roomId(newChatRoom)
                    .member(buyer)
                    .build();

            ChatRoomMember sellerMember = ChatRoomMember.builder()
                    .roomId(newChatRoom)
                    .member(workshop)
                    .build();

            chatRoomMemberRepository.save(buyerMember);
            chatRoomMemberRepository.save(sellerMember);

            log.info("[chat] create chatroom success");

            return ResponseEntity.status(ResultCode.OK.getCode()).body(newChatRoom.getId());
        } else {
            log.warn("[chat] user is not found!");
            return ResultCode.NOT_FOUND_USER.toResponseEntity();
        }
    }

    @Override
    public ResponseEntity<?> deleteChatRoom(Long roomId, String nickname) {
        log.info("[chat] finding chatroom...");
        Optional<ChatRoom> chatRoomList = chatRoomRepository.findById(roomId);
        User foundUser = userRepository.findByNickName(nickname);
        if(foundUser == null){
            return ResultCode.NOT_FOUND_USER.toResponseEntity();
        }
        if(!(foundUser.isEnabled())){
            return ResultCode.DELETED_USER.toResponseEntity();
        }

        if (!chatRoomList.isEmpty()) {
            ChatRoom findChatRoom = chatRoomList.get();
            List<ChatRoomMember> chatRoomMemberList = chatRoomMemberRepository.findAllByRoomId(findChatRoom);

            for (ChatRoomMember tmp : chatRoomMemberList) {
                if(tmp.getMember().equals(foundUser)){
                    tmp.setMember(null);
                    tmp.setRoomId(null);
                    chatRoomMemberRepository.delete(tmp); // 본인만 삭제
                }
            }

            if(!chatRoomMemberRepository.existsChatRoomMemberByRoomId(findChatRoom)){ // 둘 다 채팅방을 나간 경우
                log.info("[chat] buyer and seller already exit room!");
                log.info("[chat] delete chatroom!");

                try{
                    chatContentsRepository.deleteAllByChatRoomId(findChatRoom.getId());
                    log.info("[chat] delete chat contents successfully!");
                }catch (Exception e) {
                    e.printStackTrace();
                    log.error("[chat] fail to delete chat contents!");
                }
                try{
                    chatRoomRepository.delete(findChatRoom);
                    log.info("[chat] delete chat room successfully!");
                }catch (Exception e){
                    e.printStackTrace();
                    log.error("[chat] fail to delete chatroom!");
                }
            }
            return ResponseEntity.status(ResultCode.OK.getCode()).body("성공적으로 채팅방을 삭제하였습니다.");
        } else {
            return ResponseEntity.status(ResultCode.FAIL.getCode()).body("해당 채팅방을 찾을 수 없습니다.");
        }

    }


    @Override
    public ResponseChatDto saveMessage(RequestChatContentsDto requestChatContentsDto, Long roomId, String nickname) {
        User user = userRepository.findByNickName(nickname);
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).get();

        List<ChatRoomMember> chatRoomMemberList = chatRoomMemberRepository.findAllByRoomId(chatRoom);

        chatRoomMemberList.stream().map(v ->{
            User tmp = v.getMember();
            if(!tmp.equals(user)){
                RequestFCMSendDto dto = RequestFCMSendDto.builder()
                        .senderNickname(tmp)
                        .body(requestChatContentsDto.getContents())
                        .build();
                try{
                    sendMessageTo(dto);
                }catch (Exception e){
                    e.printStackTrace();
                    log.error("[chat] fail to send notification!");
                }

            }
            return null;
        });

        // 저장되는 채팅 내역
        ChatContents chatContents = ChatContents.builder()
                .sendTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd a HH시 mm분 ss초")))
                .sender(user)
                .message(requestChatContentsDto.getContents())
                .chatRoomId(roomId)
                .build();

        // 실제 전송되는 메세지
        ResponseChatDto responseChatDto = ResponseChatDto.builder()
                .contents(chatContents.getMessage())
                .nickName(user.getNickName())
                .time(chatContents.getSendTime())
                .build();

        chatContentsRepository.save(chatContents);

        return responseChatDto;
    }

    @Override
    public Page<ResponseChatContentsDto> getChatContents(String nickname, Pageable pageable, Long roomNumber) {
        log.info("[chat] get chat contents room number : {}",roomNumber);
        User user = userRepository.findByNickName(nickname);
        Optional<ChatRoom> chatRoomList = chatRoomRepository.findById(roomNumber);
        if(!chatRoomList.isEmpty()){
            ChatRoom chatRoom = chatRoomList.get();
            if(checkMember(chatRoom,user)){
                Page<ChatContents> contentsList = chatContentsRepository.findByChatRoomId(roomNumber,pageable);

                List<ResponseChatContentsDto> contentsDtoList = contentsList.stream()
                        .map(v -> {
                            ResponseChatContentsDto dto = new ResponseChatContentsDto();
                            dto.setSendTime(v.getSendTime());
                            dto.setSender(v.getSender().getNickName());
                            dto.setMessage(v.getMessage());
                            dto.setChatRoomId(v.getChatRoomId());
                            return dto;

                        }).collect(Collectors.toList());

                return new PageImpl<>(contentsDtoList, pageable, contentsList.getTotalElements());

            }else{
                throw new NotMemberException("채팅방 조회 권한이 없습니다.");
            }
        }else{
            throw new ChatRoomNotFoundException("채팅방이 존재하지 않습니다.");
        }
    }

    @Override
    public Page<ResponseMyChatRoomsDto> getMyRooms(String nickname, Pageable pageable) {
        log.info("[chat] get" + "{}" + "chat rooms",nickname);
        User user = userRepository.findByNickName(nickname);
        if(!(user == null)){
            List<ChatRoomMember> chatRoomMemberList = chatRoomMemberRepository.findAllByMember(user);
            if(chatRoomMemberList.isEmpty()){
                    return Page.empty(pageable);
                }
            log.info("[chat] chatroom is exist!");
            List<Long> roomIds = chatRoomMemberList.stream().map(chatRoomMember -> chatRoomMember.getRoomId().getId())
                    .collect(Collectors.toList());
            log.info("[chat] collect my chat rooms..");

            Page<ChatRoom> chatRooms = chatRoomRepository.findByIdIn(roomIds, pageable);

            List<ResponseMyChatRoomsDto> chatRoomsDtoList = chatRooms.stream()
                    .map(chatRoom -> {
                        ResponseMyChatRoomsDto dto = new ResponseMyChatRoomsDto();
                        dto.setId(chatRoom.getId());
                        dto.setRoomName(chatRoom.getRoomName());
                        dto.setUpdateAt(chatRoom.getModifiedDate());
                        return dto;
                    }).collect(Collectors.toList());

            return new PageImpl<>(chatRoomsDtoList, pageable, chatRooms.getTotalElements());

            }else{
            log.error("[chat] not found user");
            throw new NotMemberException();
        }
    }

    private void sendMessageTo(RequestFCMSendDto requestFCMSendDto) {
        log.info("[CHATTING-NOTIFICATION] send to {}", requestFCMSendDto.getSenderNickname());

        User user = requestFCMSendDto.getSenderNickname();
        String fcmToken = user.getFCMToken();

        log.info("[CHATTING-NOTIFICATION] fcm token : {}", fcmToken);

        try {
            Notification notification = Notification.builder()
                    .setTitle("GAGU")
                    .setBody(requestFCMSendDto.getBody())
                    .build();

            Message message = Message.builder()
                    .setToken(user.getFCMToken())
                    .setNotification(notification)
                    .build();

            firebaseMessaging.send(message);

        }catch (Exception e){
            e.printStackTrace();
            log.error("[CHATTING-NOTIFICATION] fail to send notification!");
        }
    }


    private boolean checkUserExist(String userEmail, String userNickname){
        log.info("[chat] check user exist");
        return userRepository.existsByEmailAndNickName(userEmail, userNickname);
    }
    private boolean checkWorkshopExist(String workShopName){
        log.info("[chat] check workshop exist");
        return userRepository.existsByNickName(workShopName);
    }
    private boolean checkChatRoomExist(ChatRoom chatroom){
        return chatRoomMemberRepository.existsChatRoomMemberByRoomId(chatroom);
    }
    private String createChatRoomName(String buyerName, String sellerName){
        return buyerName + "님과 " + sellerName + "과의 채팅방";
    }
    public boolean checkMember(ChatRoom roomId, User member){
        return chatRoomMemberRepository.existsChatRoomMemberByRoomIdAndMember(roomId,member);
    }
    public boolean areUsersInSameRoom(User user, User workshop) {
        return chatRoomMemberRepository.existsChatRoomByMembers(user, workshop);
    }
}

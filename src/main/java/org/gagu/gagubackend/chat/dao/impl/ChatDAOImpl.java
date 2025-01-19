package org.gagu.gagubackend.chat.dao.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.gagu.gagubackend.estimate.domain.Estimate;
import org.gagu.gagubackend.estimate.repository.EstimateRepository;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.gagu.gagubackend.global.exception.ChatRoomNotFoundException;
import org.gagu.gagubackend.global.exception.NotFoundUserException;
import org.gagu.gagubackend.global.exception.NotMemberException;
import org.gagu.gagubackend.auth.domain.User;
import org.gagu.gagubackend.auth.dto.request.RequestUserInfoDto;
import org.gagu.gagubackend.auth.repository.UserRepository;
import org.gagu.gagubackend.global.util.TimeUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

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
    private final EstimateRepository estimateRepository;
    private final FirebaseMessaging firebaseMessaging;
    private final TimeUtil timeUtil;

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
                return ResponseEntity.status(ResultCode.OK.getCode()).body(roomId);
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
        Optional<User> userOptional = userRepository.findUserByNickname(nickname);
        if(userOptional.isPresent()){
            String contents = requestChatContentsDto.getChatContentsInfo().getContents();
            User user = userOptional.get();
            // 저장되는 채팅 내역
            ChatContents chatContents = new ChatContents(timeUtil.makeTimeTemplate(),
                    contents,
                    roomId,
                    requestChatContentsDto.getType().toString(),
                    user);

            // 실제 전송되는 메세지
            ResponseChatDto responseChatDto = ResponseChatDto.builder()
                    .type(requestChatContentsDto.getType())
                    .chatContentInfo(new ResponseChatDto.ChatContentInfo(contents))
                    .nickName(user.getNickName())
                    .time(chatContents.getSendTime())
                    .build();

            chatContentsRepository.save(chatContents);

            return responseChatDto;
        }else{
            throw new NotFoundUserException();
        }
    }

    @Override
    public Page<ResponseChatContentsDto> getChatContents(String nickname, Pageable pageable, Long roomNumber) {
        log.info("[GET-CHAT-CONTENTS] room number : {}", roomNumber);
            Page<ChatContents> contentsList = chatContentsRepository.pageChatContents(roomNumber,pageable);

            List<ResponseChatContentsDto> contentsDtoList = contentsList.stream()
                        .map(v -> {
                            String messageType = v.getMessageType();
                            ResponseChatContentsDto dto = new ResponseChatContentsDto();
                            switch (messageType) {
                                case "SEND":
                                    dto.generalBuilder(v);
                                    break;
                                case "RESPONSE_ESTIMATE":
                                    Optional<Estimate> optionalEstimate = estimateRepository.findEstimateById(v.getEstimateId());
                                    if(optionalEstimate.isPresent()){
                                        dto.resEstimateBuilder(v, optionalEstimate.get());
                                        break;
                                    }else{
                                        dto.generalBuilder(v);
                                        break;
                                    }
                                case "REQUEST_ESTIMATE":
                                    optionalEstimate = estimateRepository.findEstimateById(v.getEstimateId());
                                    if(optionalEstimate.isPresent()){
                                        dto.reqEstimateBuilder(v, optionalEstimate.get());
                                        break;
                                    }else{
                                        dto.generalBuilder(v);
                                        break;
                                    }
                            }
                        return dto;
                        }).collect(Collectors.toList());
            return new PageImpl<>(contentsDtoList, pageable, contentsList.getTotalElements());
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
    @Override
    public void sendMessageTo(RequestFCMSendDto requestFCMSendDto) {
        log.info("[CHATTING-NOTIFICATION] send to {}", requestFCMSendDto.getSenderNickname().getNickName());

        User user = requestFCMSendDto.getSenderNickname();
        String fcmToken = user.getFCMToken();

        log.info("[CHATTING-NOTIFICATION] fcm token : {}", fcmToken);

        try {
            Notification notification = Notification.builder()
                    .setTitle("GAGU 채팅 알림")
                    .setBody(requestFCMSendDto.getBody())
                    .build();

            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification)
                    .build();

            firebaseMessaging.send(message);

        }catch (Exception e){
            e.printStackTrace();
            log.error("[CHATTING-NOTIFICATION] fail to send notification!");
        }
    }

    @Override
    public Optional<ChatRoom> getChatRoomByRoomId(Long id) {
        return chatRoomRepository.findChatRoomByRoomId(id);
    }

    @Override
    public Optional<ChatRoomMember> getChatRoomMember(String nickname, Long id) {
        return chatRoomMemberRepository.getChatRoomMemberByRoomIdAndUser(id, nickname);
    }

    @Override
    public ResponseChatDto askEstimate(RequestChatContentsDto message, Long roomNumber, String nickname) {
        Long estimateId = message.getEstimateInfo().getEstimateId();
        Optional<Estimate> estimateOptional = estimateRepository.findEstimateById(estimateId);
        if(estimateOptional.isPresent()){
            User user = userRepository.findUserByNickname(nickname).get();
            String time = timeUtil.makeTimeTemplate();

            chatContentsRepository.save(new ChatContents(time, " ", roomNumber, message.getType().toString(), estimateId, user));

            return ResponseChatDto.builder()
                    .type(message.getType())
                    .estimateInfo(new ResponseChatDto.EstimateInfo(estimateOptional.get()))
                    .nickName(nickname)
                    .time(time)
                    .build();
        }else{
            throw new NullPointerException("견적서를 찾을 수 없습니다.");
        }
    }

    @Override
    public ResponseChatDto completeEstimate(RequestChatContentsDto message, Long roomNumber, String nickname) {
        RequestChatContentsDto.EstimateInfo estimateInfo = message.getEstimateInfo();
        Long estimateId = estimateInfo.getEstimateId();
        Optional<Estimate> estimateOptional = estimateRepository.findEstimateById(estimateId);

        if(estimateOptional.isPresent()){
            String time = timeUtil.makeTimeTemplate();
            User user = userRepository.findUserByNickname(nickname).get();
            chatContentsRepository.save(new ChatContents(time, " ", roomNumber, message.getType().toString(), estimateId, user));

            Estimate estimate = estimateOptional.get();
            estimate.setMakerName(nickname);

            estimateRepository.save(estimate);
            return ResponseChatDto.builder()
                    .type(message.getType())
                    .estimateInfo(new ResponseChatDto.EstimateInfo(estimateOptional.get()))
                    .nickName(nickname)
                    .time(time)
                    .build();
        }else{
            log.error("[CHAT-DAO-IMPL] fail to get estimate info!");
            throw new NullPointerException("견적서를 찾을 수 없습니다.");
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
    private String createChatRoomName(String buyerName, String sellerName){
        return buyerName + "님과 " + sellerName + "과의 채팅방";
    }
    public boolean areUsersInSameRoom(User user, User workshop) {
        return chatRoomMemberRepository.existsChatRoomByMembers(user, workshop);
    }
}

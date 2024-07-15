package org.gagu.gagubackend.chat.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.chat.dao.ChatDAO;
import org.gagu.gagubackend.chat.domain.ChatContents;
import org.gagu.gagubackend.chat.domain.ChatRoom;
import org.gagu.gagubackend.chat.domain.ChatRoomMember;
import org.gagu.gagubackend.chat.dto.request.RequestChatContentsDto;
import org.gagu.gagubackend.chat.dto.request.RequestCreateChatRoomDto;
import org.gagu.gagubackend.chat.dto.response.ResponseChatDto;
import org.gagu.gagubackend.chat.dto.response.ResponseMyChatRoomsDto;
import org.gagu.gagubackend.chat.repository.ChatContentsRepository;
import org.gagu.gagubackend.chat.repository.ChatRoomMemberRepository;
import org.gagu.gagubackend.chat.repository.ChatRoomRepository;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.gagu.gagubackend.global.exception.ChatRoomNotFoundException;
import org.gagu.gagubackend.global.exception.NotMemberException;
import org.gagu.gagubackend.user.domain.User;
import org.gagu.gagubackend.user.dto.request.RequestUserInfoDto;
import org.gagu.gagubackend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
    @Override
    public ResponseEntity<?> createChatRoom(RequestUserInfoDto userInfoDto, RequestCreateChatRoomDto requestCreateChatRoomDto) {
        log.info("[chat] crate chat room");
        String userEmail = userInfoDto.getUserEmail();
        String userNickname = userInfoDto.getUserNickname();
        String workShopEmail = requestCreateChatRoomDto.getSellerEmail();
        String workShopName = requestCreateChatRoomDto.getSellerNickname();

        if (checkUserExist(userEmail,userNickname) && checkWorkshopExist(workShopEmail,workShopName)){
            User buyer = userRepository.findByEmailAndNickName(userEmail,userNickname);
            User workshop = userRepository.findByEmailAndNickName(workShopEmail,workShopName);
            String chatRoomName = createChatRoomName(userNickname,workShopName);

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

            return ResponseEntity.status(ResultCode.OK.getCode()).body("성공적으로 채팅방을 생성하였습니다.");
        }else{
            return ResponseEntity.status(ResultCode.FAIL.getCode()).body(ResultCode.NOT_FOUND_USER.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> deleteChatRoom(Long roomId) {
        log.info("[chat] find chatroom");
        Optional<ChatRoom> chatRoomList = chatRoomRepository.findById(roomId);
        if(!chatRoomList.isEmpty()){
            ChatRoom findChatRoom = chatRoomList.get();
            if(checkChatRoomExist(findChatRoom)){
                List<ChatRoomMember> chatRoomMemberList = chatRoomMemberRepository.findAllByRoomId(findChatRoom);

                for(ChatRoomMember tmp : chatRoomMemberList){
                    tmp.setMember(null);
                    tmp.setRoomId(null);

                    chatRoomMemberRepository.delete(tmp);
                }
                chatRoomRepository.delete(findChatRoom);
                return ResponseEntity.status(ResultCode.OK.getCode()).body("성공적으로 채팅방을 삭제하였습니다.");
            }else{
                return ResponseEntity.status(ResultCode.FAIL.getCode()).body("채팅방 정보가 일치하지 않습니다.");
            }

            }else{
            return ResponseEntity.status(ResultCode.FAIL.getCode()).body("해당 채팅방을 찾지 못했습니다.");
        }
    }

    @Override
    public ResponseChatDto saveMessage(RequestChatContentsDto requestChatContentsDto, Long roomId) {

        // 저장되는 채팅 내역
        ChatContents chatContents = ChatContents.builder()
                .sendTime(LocalDateTime.now())
                .sender(requestChatContentsDto.getNickname())
                .message(requestChatContentsDto.getContents())
                .chatRoomId(roomId)
                .build();

        // 실제 전송되는 메세지
        ResponseChatDto responseChatDto = ResponseChatDto.builder()
                .contents(chatContents.getMessage())
                .nickName(chatContents.getSender())
                .time(chatContents.getSendTime())
                .build();

        chatContentsRepository.save(chatContents);

        return responseChatDto;
    }

    @Override
    public Page<ChatContents> getChatContents(String nickname, Pageable pageable, Long roomNumber) {
        log.info("[chat] get chat contents room number : {}",roomNumber);
        User user = userRepository.findByNickName(nickname);
        Optional<ChatRoom> chatRoomList = chatRoomRepository.findById(roomNumber);
        if(!chatRoomList.isEmpty()){
            ChatRoom chatRoom = chatRoomList.get();
            if(checkMember(chatRoom,user)){
                return chatContentsRepository.findByChatRoomId(roomNumber,pageable);
            }else{
                throw new NotMemberException("채팅방 조회 권한이 없습니다.");
            }
        }else{
            throw new ChatRoomNotFoundException("채팅방이 존재하지 않습니다.");
        }
    }

    @Override
    public Page<ResponseMyChatRoomsDto> getChatMyRooms(String nickname, Pageable pageable) {
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
                        dto.setUpdateAt(chatRoom.getUpdatedAt());
                        return dto;
                    }).collect(Collectors.toList());

            return new PageImpl<>(chatRoomsDtoList, pageable, chatRooms.getTotalElements());

            }else{
            log.error("[chat] not found user");
            throw new NotMemberException();
        }

        }


    private boolean checkUserExist(String userEmail, String userNickname){
        log.info("[chat] check user exist");
        return userRepository.existsByEmailAndNickName(userEmail, userNickname);
    }
    private boolean checkWorkshopExist(String workShopEmail, String workShopName){
        log.info("[chat] check workshop exist");
        return userRepository.existsByEmailAndNickName(workShopEmail,workShopName);
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
}

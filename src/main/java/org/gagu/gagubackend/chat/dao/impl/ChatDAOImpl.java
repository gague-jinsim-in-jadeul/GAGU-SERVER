package org.gagu.gagubackend.chat.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.chat.dao.ChatDAO;
import org.gagu.gagubackend.chat.domain.ChatRoom;
import org.gagu.gagubackend.chat.domain.ChatRoomMember;
import org.gagu.gagubackend.chat.dto.request.RequestCreateChatRoomDto;
import org.gagu.gagubackend.chat.repository.ChatRoomMemberRepository;
import org.gagu.gagubackend.chat.repository.ChatRoomRepository;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.gagu.gagubackend.user.domain.User;
import org.gagu.gagubackend.user.dto.request.RequestUserInfoDto;
import org.gagu.gagubackend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ChatDAOImpl implements ChatDAO {
    private final UserRepository userRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Autowired
    public ChatDAOImpl(UserRepository userRepository, ChatRoomMemberRepository chatRoomMemberRepository, ChatRoomRepository chatRoomRepository) {
        this.userRepository = userRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.chatRoomRepository = chatRoomRepository;
    }

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
}

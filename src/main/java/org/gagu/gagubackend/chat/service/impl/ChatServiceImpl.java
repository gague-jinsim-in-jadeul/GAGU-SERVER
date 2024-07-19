package org.gagu.gagubackend.chat.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.chat.dao.ChatDAO;
import org.gagu.gagubackend.chat.domain.ChatContents;
import org.gagu.gagubackend.chat.domain.ChatRoom;
import org.gagu.gagubackend.chat.dto.request.RequestChatContentsDto;
import org.gagu.gagubackend.chat.dto.request.RequestCreateChatRoomDto;
import org.gagu.gagubackend.chat.dto.response.ResponseChatDto;
import org.gagu.gagubackend.chat.dto.response.ResponseMyChatRoomsDto;
import org.gagu.gagubackend.chat.service.ChatService;
import org.gagu.gagubackend.user.dto.request.RequestUserInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {
    private final ChatDAO chatDAO;

    @Autowired
    public ChatServiceImpl(ChatDAO chatDAO) {
        this.chatDAO = chatDAO;
    }

    @Override
    public ResponseEntity<?> createChatRoom(RequestUserInfoDto userInfoDto, RequestCreateChatRoomDto requestCreateChatRoomDto) {
        return chatDAO.createChatRoom(userInfoDto, requestCreateChatRoomDto);
    }

    @Override
    public ResponseEntity<?> exitChatRoom(Long roomNumber) {
        return chatDAO.deleteChatRoom(roomNumber);
    }

    @Override
    public ResponseChatDto sendContents(RequestChatContentsDto message, Long roomNumber) {
        String messageType = message.getType().toString();
        ResponseChatDto responseChatDto = new ResponseChatDto();

        switch (messageType){
            case "SEND":
                return chatDAO.saveMessage(message,roomNumber);

            case "SUBSCRIBE":
                responseChatDto.setContents(message.getNickname()+" 님이 들어오셨습니다.");
                responseChatDto.setTime(LocalDateTime.now());
                responseChatDto.setNickName(message.getNickname());

                return responseChatDto;
        }
        return null;
    }

    @Override
    public Page<ChatContents> getChatContents(String nickname, Pageable pageable, Long roomNumber) {
        return chatDAO.getChatContents(nickname,pageable,roomNumber);
    }

    @Override
    public Page<ResponseMyChatRoomsDto> getMyChatRooms(String nickname, Pageable pageable) {
        return chatDAO.getMyRooms(nickname, pageable);
    }
}

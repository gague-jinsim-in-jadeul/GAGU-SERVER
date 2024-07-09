package org.gagu.gagubackend.chat.dao;

import org.gagu.gagubackend.chat.domain.ChatContents;
import org.gagu.gagubackend.chat.dto.request.RequestChatContentsDto;
import org.gagu.gagubackend.chat.dto.request.RequestCreateChatRoomDto;
import org.gagu.gagubackend.chat.dto.response.ResponseChatDto;
import org.gagu.gagubackend.user.dto.request.RequestUserInfoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface ChatDAO {
    ResponseEntity<?> createChatRoom(RequestUserInfoDto userInfoDto, RequestCreateChatRoomDto requestCreateChatRoomDto);
    ResponseEntity<?> deleteChatRoom(Long roomNumber);
    ResponseChatDto saveMessage(RequestChatContentsDto requestChatContentsDto, Long roomNumber);
    Page<ChatContents> getChatContents(String nickname, Pageable pageable, Long roomNumber);

}

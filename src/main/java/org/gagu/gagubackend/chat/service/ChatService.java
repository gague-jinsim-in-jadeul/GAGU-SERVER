package org.gagu.gagubackend.chat.service;

import org.gagu.gagubackend.chat.domain.ChatContents;
import org.gagu.gagubackend.chat.dto.request.RequestChatContentsDto;
import org.gagu.gagubackend.chat.dto.request.RequestCreateChatRoomDto;
import org.gagu.gagubackend.chat.dto.response.ResponseChatDto;
import org.gagu.gagubackend.user.dto.request.RequestUserInfoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface ChatService {
    /**
     *
     * @param userInfoDto
     * @param requestCreateChatRoomDto
     * @return
     */
    ResponseEntity<?> createChatRoom(RequestUserInfoDto userInfoDto, RequestCreateChatRoomDto requestCreateChatRoomDto);

    /**
     *
     * @param roomNumber 채팅방 id 값
     * @return
     */
    ResponseEntity<?> exitChatRoom(Long roomNumber);

    /**
     *
     * @param message
     * @param roomNumber 채팅방 id 값
     * @return ResponseChatDto
     */
    ResponseChatDto sendContents(RequestChatContentsDto message, Long roomNumber);

    Page<ChatContents> getChatContents(String nickname, Pageable pageable, Long roomId);
}

package org.gagu.gagubackend.chat.service;

import org.gagu.gagubackend.chat.dto.request.RequestCreateChatRoomDto;
import org.gagu.gagubackend.user.dto.request.RequestUserInfoDto;
import org.springframework.http.ResponseEntity;

public interface ChatService {
    ResponseEntity<?> createChatRoom(RequestUserInfoDto userInfoDto, RequestCreateChatRoomDto requestCreateChatRoomDto);
    ResponseEntity<?> exitChatRoom(Long roomId);
}

package org.gagu.gagubackend.chat.dao;

import org.gagu.gagubackend.chat.dto.request.RequestChatRoomDto;
import org.gagu.gagubackend.chat.dto.request.RequestCreateChatRoomDto;
import org.gagu.gagubackend.user.dto.request.RequestUserInfoDto;
import org.springframework.http.ResponseEntity;

public interface ChatDAO {
    ResponseEntity<?> createChatRoom(RequestUserInfoDto userInfoDto, RequestCreateChatRoomDto requestCreateChatRoomDto);
    ResponseEntity<?> deleteChatRoom(Long roomId);

}

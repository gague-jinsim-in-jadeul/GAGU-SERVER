package org.gagu.gagubackend.chat.service;

import org.gagu.gagubackend.chat.domain.ChatContents;
import org.gagu.gagubackend.chat.domain.ChatRoom;
import org.gagu.gagubackend.chat.dto.request.RequestChatContentsDto;
import org.gagu.gagubackend.chat.dto.request.RequestCreateChatRoomDto;
import org.gagu.gagubackend.chat.dto.response.ResponseChatDto;
import org.gagu.gagubackend.chat.dto.response.ResponseMyChatRoomsDto;
import org.gagu.gagubackend.user.dto.request.RequestUserInfoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface ChatService {
    /**
     * 사용자 + 공방관계자 채팅방 생성
     * @param userInfoDto
     * @param requestCreateChatRoomDto
     * @return
     */
    ResponseEntity<?> createChatRoom(RequestUserInfoDto userInfoDto, RequestCreateChatRoomDto requestCreateChatRoomDto);

    /**
     * 채팅방 삭제
     * @param roomNumber 채팅방 id 값
     * @return
     */
    ResponseEntity<?> exitChatRoom(Long roomNumber);

    /**
     * 메세지 전송
     * @param message
     * @param roomNumber 채팅방 id 값
     * @return ResponseChatDto
     */
    ResponseChatDto sendContents(RequestChatContentsDto message, Long roomNumber);

    /**
     * 채팅방 내역 페이징 처리 후 반환
     * @param nickname
     * @param pageable
     * @param roomId
     * @return
     */
    Page<ChatContents> getChatContents(String nickname, Pageable pageable, Long roomId);

    /**
     * 내가 속한 채팅방들 반환
     * @param nickname
     * @return 채팅방
     */
    Page<ResponseMyChatRoomsDto> getMyChatRooms(String nickname, Pageable pageable);
}

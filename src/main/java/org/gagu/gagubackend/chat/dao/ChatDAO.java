package org.gagu.gagubackend.chat.dao;

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

public interface ChatDAO {
    /**
     * 사용자 + 공방관계자 채팅방 생성
     * @param userInfoDto
     * @param requestCreateChatRoomDto
     * @return 상태반환
     */
    ResponseEntity<?> createChatRoom(RequestUserInfoDto userInfoDto, RequestCreateChatRoomDto requestCreateChatRoomDto);

    /**
     * 채팅방 삭제
     * @param roomNumber
     * @return 상태반환
     */
    ResponseEntity<?> deleteChatRoom(Long roomNumber);

    /**
     * 메세지 전송 후 저장
     * @param requestChatContentsDto
     * @param roomNumber
     * @return dto
     */
    ResponseChatDto saveMessage(RequestChatContentsDto requestChatContentsDto, Long roomNumber);

    /**
     * 채팅방 내용 반환
     * @param nickname
     * @param pageable
     * @param roomNumber
     * @return page
     */
    Page<ChatContents> getChatContents(String nickname, Pageable pageable, Long roomNumber);

    Page<ResponseMyChatRoomsDto> getChatMyRooms(String nickname, Pageable pageable);

}

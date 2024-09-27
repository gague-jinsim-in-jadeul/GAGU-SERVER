package org.gagu.gagubackend.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.gagu.gagubackend.chat.domain.ChatContents;
import org.gagu.gagubackend.chat.dto.request.RequestChatContentsDto;
import org.gagu.gagubackend.chat.dto.request.RequestCreateChatRoomDto;
import org.gagu.gagubackend.chat.dto.request.RequestFCMSendDto;
import org.gagu.gagubackend.chat.dto.response.ResponseChatDto;
import org.gagu.gagubackend.chat.dto.response.ResponseImageDto;
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
     * 둘 다 채팅방을 삭제 하면 채팅 내역이 사라집니다.
     * @param roomNumber 채팅방 id 값
     * @return
     */
    ResponseEntity<?> exitChatRoom(Long roomNumber, String nickname);

    /**
     * 메세지 전송
     * @param message
     * @param roomNumber 채팅방 id 값
     * @return ResponseChatDto
     */
    ResponseChatDto sendContents(RequestChatContentsDto message, Long roomNumber, String nickname) throws JsonProcessingException;

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

    /**
     * LLM 2D 이미지 반환
     *
     * @param message
     * @return jpeg
     */
    ResponseImageDto generate2D(RequestChatContentsDto message) throws JsonProcessingException;

    int sendMessageTo(RequestFCMSendDto requestFCMSendDto, String nickname);
}

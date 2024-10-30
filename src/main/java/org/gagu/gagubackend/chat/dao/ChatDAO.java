package org.gagu.gagubackend.chat.dao;

import org.gagu.gagubackend.chat.domain.ChatContents;
import org.gagu.gagubackend.chat.dto.request.RequestChatContentsDto;
import org.gagu.gagubackend.chat.dto.request.RequestCreateChatRoomDto;
import org.gagu.gagubackend.chat.dto.request.RequestFCMSendDto;
import org.gagu.gagubackend.chat.dto.response.ResponseChatContentsDto;
import org.gagu.gagubackend.chat.dto.response.ResponseChatDto;
import org.gagu.gagubackend.chat.dto.response.ResponseMyChatRoomsDto;
import org.gagu.gagubackend.auth.dto.request.RequestUserInfoDto;
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
     * 둘 다 채팅방을 삭제 하면 채팅 내역이 사라집니다.
     * @param roomNumber
     * @return 상태반환
     */
    ResponseEntity<?> deleteChatRoom(Long roomNumber, String nickname);

    /**
     * 메세지 전송 후 저장
     * @param requestChatContentsDto
     * @param roomNumber
     * @return dto
     */
    ResponseChatDto saveMessage(RequestChatContentsDto requestChatContentsDto, Long roomNumber, String nickname);

    /**
     * 채팅방 내용 반환
     * @param nickname
     * @param pageable
     * @param roomNumber
     * @return page
     */
    Page<ResponseChatContentsDto> getChatContents(String nickname, Pageable pageable, Long roomNumber);

    /**
     * 사용자가 속한 채팅방을 조회
     * @param nickname
     * @param pageable
     * @return
     */
    Page<ResponseMyChatRoomsDto> getMyRooms(String nickname, Pageable pageable);

    /**
     * 알림 푸쉬 전송
     * @param requestFCMSendDto
     */
    void sendMessageTo(RequestFCMSendDto requestFCMSendDto);

}

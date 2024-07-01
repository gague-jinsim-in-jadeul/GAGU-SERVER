package org.gagu.gagubackend.chat.repository;

import org.gagu.gagubackend.chat.domain.ChatRoom;
import org.gagu.gagubackend.chat.domain.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    boolean existsChatRoomMemberByRoomId(ChatRoom chatRoom);
    List<ChatRoomMember> findAllByRoomId(ChatRoom chatRoom);
}

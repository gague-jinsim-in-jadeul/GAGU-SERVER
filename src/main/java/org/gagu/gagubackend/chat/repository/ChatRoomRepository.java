package org.gagu.gagubackend.chat.repository;

import org.gagu.gagubackend.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {
}

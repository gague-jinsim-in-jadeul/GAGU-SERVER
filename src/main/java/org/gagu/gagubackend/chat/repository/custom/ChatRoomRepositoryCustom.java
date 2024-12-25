package org.gagu.gagubackend.chat.repository.custom;

import org.gagu.gagubackend.chat.domain.ChatRoom;

import java.util.Optional;

public interface ChatRoomRepositoryCustom {
    Optional<ChatRoom> findChatRoomByRoomId(Long id);
}

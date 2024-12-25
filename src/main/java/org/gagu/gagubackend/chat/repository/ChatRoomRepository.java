package org.gagu.gagubackend.chat.repository;

import org.gagu.gagubackend.chat.domain.ChatRoom;
import org.gagu.gagubackend.chat.repository.custom.ChatRoomRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long>, ChatRoomRepositoryCustom {
    Page<ChatRoom> findByIdIn(List<Long> roomIds, Pageable pageable);
    List<ChatRoom> findAllByRoomNameContains(String nickname);
}

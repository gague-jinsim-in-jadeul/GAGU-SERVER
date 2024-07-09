package org.gagu.gagubackend.chat.repository;

import org.gagu.gagubackend.chat.domain.ChatContents;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatContentsRepository extends JpaRepository<ChatContents, Long> {
    Page<ChatContents> findByChatRoomId(Long roomNumber, Pageable pageable);
}

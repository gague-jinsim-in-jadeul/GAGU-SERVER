package org.gagu.gagubackend.chat.repository;

import jakarta.transaction.Transactional;
import org.gagu.gagubackend.chat.domain.ChatContents;
import org.gagu.gagubackend.chat.repository.custom.ChatContentsRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatContentsRepository extends JpaRepository<ChatContents, Long>, ChatContentsRepositoryCustom {
    @Transactional
    void deleteAllByChatRoomId(Long roomNumber);
}

package org.gagu.gagubackend.chat.repository;

import org.gagu.gagubackend.chat.domain.ChatContents;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatContentsRepository extends JpaRepository<ChatContents, Long> {
}

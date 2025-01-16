package org.gagu.gagubackend.chat.repository.custom;

import org.gagu.gagubackend.chat.domain.ChatContents;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatContentsRepositoryCustom {
    Page<ChatContents> pageChatContents(Long roomNumber,  Pageable pageable);
}

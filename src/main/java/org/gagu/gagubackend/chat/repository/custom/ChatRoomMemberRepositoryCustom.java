package org.gagu.gagubackend.chat.repository.custom;

import org.gagu.gagubackend.chat.domain.ChatRoomMember;

import java.util.Optional;

public interface ChatRoomMemberRepositoryCustom {
    Optional<ChatRoomMember> getChatRoomMemberByRoomIdAndUser(Long roomId, String nickname);
}

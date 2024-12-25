package org.gagu.gagubackend.chat.repository.custom.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.gagu.gagubackend.chat.domain.ChatRoomMember;
import org.gagu.gagubackend.chat.domain.QChatRoomMember;
import org.gagu.gagubackend.chat.repository.custom.ChatRoomMemberRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ChatRoomMemberRepositoryCustomImpl implements ChatRoomMemberRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;
    @Autowired
    public ChatRoomMemberRepositoryCustomImpl(EntityManager em) {
        this.jpaQueryFactory = new JPAQueryFactory(em);
    }
    @Override
    public Optional<ChatRoomMember> getChatRoomMemberByRoomIdAndUser(Long roomId, String nickname) {
        QChatRoomMember qChatRoomMember = QChatRoomMember.chatRoomMember;

        return Optional.ofNullable(
                jpaQueryFactory.select(qChatRoomMember)
                        .from(qChatRoomMember)
                        .where(qChatRoomMember.member.nickName.eq(nickname).and(qChatRoomMember.roomId.id.eq(roomId)))
                        .fetchOne());
    }
}

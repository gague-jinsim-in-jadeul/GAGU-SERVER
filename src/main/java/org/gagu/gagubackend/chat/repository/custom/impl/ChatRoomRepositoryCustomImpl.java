package org.gagu.gagubackend.chat.repository.custom.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.gagu.gagubackend.chat.domain.ChatRoom;
import org.gagu.gagubackend.chat.domain.QChatRoom;
import org.gagu.gagubackend.chat.repository.custom.ChatRoomRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ChatRoomRepositoryCustomImpl implements ChatRoomRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    @Autowired
    public ChatRoomRepositoryCustomImpl(EntityManager em) {
        this.jpaQueryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Optional<ChatRoom> findChatRoomByRoomId(Long id) {
        QChatRoom qChatRoom = QChatRoom.chatRoom1;
        return Optional.ofNullable(jpaQueryFactory
                .select(qChatRoom)
                .from(qChatRoom)
                .where(qChatRoom.id.eq(id))
                .fetchOne());
    }
}

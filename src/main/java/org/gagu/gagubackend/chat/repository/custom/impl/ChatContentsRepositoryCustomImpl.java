package org.gagu.gagubackend.chat.repository.custom.impl;

import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.gagu.gagubackend.chat.domain.ChatContents;
import org.gagu.gagubackend.chat.domain.ChatRoomMember;
import org.gagu.gagubackend.chat.domain.QChatContents;
import org.gagu.gagubackend.chat.repository.custom.ChatContentsRepositoryCustom;
import org.gagu.gagubackend.chat.repository.custom.ChatRoomMemberRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatContentsRepositoryCustomImpl implements ChatContentsRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    public ChatContentsRepositoryCustomImpl(EntityManager em) {
        this.jpaQueryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<ChatContents> pageChatContents(Long roomNumber, Pageable pageable) {
        QChatContents qChatContents = QChatContents.chatContents;

        int page = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();

        long offset = (long) page * pageSize;

        List<ChatContents> chatContentsList = jpaQueryFactory.select(qChatContents)
                .from(qChatContents)
                .where(qChatContents.chatRoomId.eq(roomNumber))
                .orderBy(qChatContents.sendTime.desc())
                .limit(pageable.getPageSize())
                .offset(offset)
                .fetch();

        JPQLQuery<Long> count = jpaQueryFactory.select(qChatContents.count())
                .from(qChatContents)
                .where(qChatContents.chatRoomId.eq(roomNumber));

        return PageableExecutionUtils.getPage(chatContentsList, pageable, count::fetchOne);
    }
}

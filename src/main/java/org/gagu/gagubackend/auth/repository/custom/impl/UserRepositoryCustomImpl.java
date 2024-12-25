package org.gagu.gagubackend.auth.repository.custom.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.gagu.gagubackend.auth.domain.QUser;
import org.gagu.gagubackend.auth.domain.User;
import org.gagu.gagubackend.auth.repository.custom.UserRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Autowired
    public UserRepositoryCustomImpl(EntityManager em) {
        this.jpaQueryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Optional<User> findUserByNickname(String nickname) {
        QUser qUser = QUser.user;

        return Optional.ofNullable(jpaQueryFactory.select(qUser)
                .from(qUser)
                .where(qUser.nickName.eq(nickname))
                .fetchOne());
    }
}

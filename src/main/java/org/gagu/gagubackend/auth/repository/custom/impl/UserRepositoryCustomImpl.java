package org.gagu.gagubackend.auth.repository.custom.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.gagu.gagubackend.auth.domain.QUser;
import org.gagu.gagubackend.auth.domain.User;
import org.gagu.gagubackend.auth.repository.custom.UserRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
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

    @Override
    public Optional<User> checkWorkshopExist(String nickname, String loginType) {
        QUser qUser = QUser.user;
        return Optional.ofNullable(jpaQueryFactory.select(qUser)
                .from(qUser)
                .where(qUser.nickName.eq(nickname).and(qUser.loginType.eq(loginType)))
                .fetchOne());
    }

    @Override
    public Optional<List<User>> findWorkshops(String email, String type) {
        QUser qUser = QUser.user;

        return Optional.ofNullable(jpaQueryFactory.select(qUser)
                .from(qUser)
                .where(qUser.email.eq(email).and(qUser.loginType.eq(type)))
                .fetch());
    }

    @Override
    public Optional<User> checkSocialUserExist(String resourceId, String loginType) {
        QUser qUser = QUser.user;
        return Optional.ofNullable(jpaQueryFactory.select(qUser)
                .from(qUser)
                .where(qUser.resourceId.eq(resourceId).and(qUser.loginType.eq(loginType)))
                .fetchOne());
    }

    @Override
    public Optional<User> findWorkshopById(Long id) {
        QUser qUser = QUser.user;

        return Optional.ofNullable(jpaQueryFactory.select(qUser)
                .from(qUser)
                .where(qUser.id.eq(id))
                .fetchOne());
    }
}

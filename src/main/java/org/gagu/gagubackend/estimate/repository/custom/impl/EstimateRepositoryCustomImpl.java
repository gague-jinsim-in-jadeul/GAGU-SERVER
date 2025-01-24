package org.gagu.gagubackend.estimate.repository.custom.impl;

import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.gagu.gagubackend.auth.domain.User;
import org.gagu.gagubackend.estimate.domain.Estimate;
import org.gagu.gagubackend.estimate.domain.QEstimate;
import org.gagu.gagubackend.estimate.repository.custom.EstimateRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class EstimateRepositoryCustomImpl implements EstimateRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    public EstimateRepositoryCustomImpl(EntityManager em) {
        this.jpaQueryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Optional<Estimate> findEstimateById(Long id) {
        QEstimate qEstimate = QEstimate.estimate;
        return Optional.ofNullable(jpaQueryFactory
                .select(qEstimate)
                .from(qEstimate)
                .where(qEstimate.id.eq(id))
                .fetchOne());
    }

    @Override
    public Page<Estimate> findCompletedEstimates(User user, Pageable pageable) {
        QEstimate qEstimate = QEstimate.estimate;

        int limit = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        long offset = (long) limit * pageNumber;

        List<Estimate> estimateList = jpaQueryFactory.select(qEstimate)
                .from(qEstimate)
                .where(qEstimate.nickName.eq(user)
                        .and(qEstimate.makerName.isNotNull())
                        .and(qEstimate.price.isNotNull())
                        .and(qEstimate.description.isNotNull()))
                .offset(offset)
                .limit(limit)
                .orderBy(qEstimate.modifiedDate.desc())
                .fetch();

        JPQLQuery<Long> count = jpaQueryFactory.select(qEstimate.count())
                .from(qEstimate)
                .where(qEstimate.nickName.eq(user)
                        .and(qEstimate.makerName.isNotNull())
                        .and(qEstimate.price.isNotNull())
                        .and(qEstimate.description.isNotNull()));

        return PageableExecutionUtils.getPage(estimateList, pageable, count::fetchOne);
    }
}

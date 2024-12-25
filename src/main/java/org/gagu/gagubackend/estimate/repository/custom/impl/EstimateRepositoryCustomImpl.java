package org.gagu.gagubackend.estimate.repository.custom.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.gagu.gagubackend.estimate.domain.Estimate;
import org.gagu.gagubackend.estimate.domain.QEstimate;
import org.gagu.gagubackend.estimate.repository.custom.EstimateRepositoryCustom;
import org.springframework.stereotype.Service;

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
}

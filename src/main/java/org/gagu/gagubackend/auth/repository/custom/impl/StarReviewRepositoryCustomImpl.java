package org.gagu.gagubackend.auth.repository.custom.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.gagu.gagubackend.auth.domain.QStarReview;
import org.gagu.gagubackend.auth.domain.StarReview;
import org.gagu.gagubackend.auth.repository.custom.StarReviewRepositoryCustom;
import org.gagu.gagubackend.global.domain.enums.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StarReviewRepositoryCustomImpl implements StarReviewRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    public StarReviewRepositoryCustomImpl(EntityManager em) {
        this.jpaQueryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<StarReview> pageStartReviews(FilterType filterType, Pageable pageable, Double myLongitude, Double myLatitude) {
        QStarReview qStarReview = QStarReview.starReview;
        List<StarReview> starReviewList = null;
        String type = filterType.toString();

        int page = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();

        long offset = (long) page * pageSize;

        switch (type){
            case "POPULAR":
                starReviewList = jpaQueryFactory.select(qStarReview)
                        .from(qStarReview)
                        .orderBy(qStarReview.count.desc(), qStarReview.starsAverage.desc())
                        .limit(pageable.getPageSize())
                        .offset(offset)
                        .fetch();
                JPQLQuery<Long> count = jpaQueryFactory.select(qStarReview.count())
                        .from(qStarReview);

                return PageableExecutionUtils.getPage(starReviewList, pageable, count::fetchOne);

            case "NEAR":
                Double distanceThreshold = 50000.0; // 50 km in meters

                // 거리 계산
                NumberExpression<Double> distance = Expressions.numberTemplate(Double.class,
                        "6371000 * acos(cos(radians({0})) * cos(radians({1})) * cos(radians({2}) - radians({3})) + sin(radians({0})) * sin(radians({1})))",
                        Expressions.constant(myLatitude),
                        qStarReview.workshop.latitude,
                        Expressions.constant(myLongitude),
                        qStarReview.workshop.longitude
                );

                starReviewList = jpaQueryFactory.select(qStarReview)
                        .from(qStarReview)
                        .where(distance.loe(distanceThreshold))
                        .orderBy(distance.asc())
                        .limit(pageable.getPageSize())
                        .offset(offset)
                        .fetch();

                count = jpaQueryFactory.select(qStarReview.count())
                        .from(qStarReview)
                        .where(distance.loe(distanceThreshold));

                return PageableExecutionUtils.getPage(starReviewList, pageable, count::fetchOne);

        }
        return null;
    }
}

package org.gagu.gagubackend.auth.repository.custom;

import org.gagu.gagubackend.auth.domain.StarReview;
import org.gagu.gagubackend.global.domain.enums.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StarReviewRepositoryCustom {
    Page<StarReview> pageStartReviews(FilterType filterType, Pageable pageable, Double longitude, Double latitude);
}

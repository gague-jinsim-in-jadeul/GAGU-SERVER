package org.gagu.gagubackend.auth.repository;

import org.gagu.gagubackend.auth.domain.StarReview;
import org.gagu.gagubackend.auth.repository.custom.StarReviewRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StarReviewRepository extends JpaRepository<StarReview, Long>, StarReviewRepositoryCustom {
    StarReview findByWorkshopName(String workshopName);
    List<StarReview> findAllByWorkshopNameContains(String workshopName);
}

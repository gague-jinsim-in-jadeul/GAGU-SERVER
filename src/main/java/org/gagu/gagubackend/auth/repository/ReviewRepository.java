package org.gagu.gagubackend.auth.repository;

import org.gagu.gagubackend.auth.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByWorkshopName(String workshopName, Pageable pageable);
}

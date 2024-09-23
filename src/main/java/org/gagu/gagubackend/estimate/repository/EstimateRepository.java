package org.gagu.gagubackend.estimate.repository;

import org.gagu.gagubackend.estimate.domain.Estimate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstimateRepository extends JpaRepository<Estimate,Long> {
    Page<Estimate> findByNickName(String nickname, Pageable pageable);
}

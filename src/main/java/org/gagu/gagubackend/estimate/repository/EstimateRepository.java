package org.gagu.gagubackend.estimate.repository;

import org.gagu.gagubackend.estimate.domain.Estimate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstimateRepository extends JpaRepository<Estimate,Long> {
}

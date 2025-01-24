package org.gagu.gagubackend.estimate.repository.custom;

import org.gagu.gagubackend.auth.domain.User;
import org.gagu.gagubackend.estimate.domain.Estimate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface EstimateRepositoryCustom {
    Optional<Estimate> findEstimateById(Long id);
    Page<Estimate> findCompletedEstimates(User user, Pageable pageable);
}

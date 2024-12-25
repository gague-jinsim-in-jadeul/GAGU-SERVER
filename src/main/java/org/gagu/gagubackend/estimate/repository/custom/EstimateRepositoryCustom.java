package org.gagu.gagubackend.estimate.repository.custom;

import org.gagu.gagubackend.estimate.domain.Estimate;

import java.util.Optional;

public interface EstimateRepositoryCustom {
    Optional<Estimate> findEstimateById(Long id);
}

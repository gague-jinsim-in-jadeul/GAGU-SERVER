package org.gagu.gagubackend.estimate.repository;

import org.gagu.gagubackend.auth.domain.User;
import org.gagu.gagubackend.estimate.domain.Estimate;
import org.gagu.gagubackend.estimate.repository.custom.EstimateRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EstimateRepository extends JpaRepository<Estimate,Long>, EstimateRepositoryCustom{
    Page<Estimate> findByNickName(User user, Pageable pageable);
    Page<Estimate> findByNickNameAndMakerName(User user, String workshop, Pageable pageable);
    List<Estimate> findAllByNickName(User user);

    List<Estimate> findAllByMakerNameContains(String nickname);

}

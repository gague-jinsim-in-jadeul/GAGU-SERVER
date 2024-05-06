package org.gagu.gagubackend.user.buyer.repository;

import org.gagu.gagubackend.user.buyer.domain.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyerRepository extends JpaRepository<Buyer,Long> {
    Buyer findByEmail(String name);
}

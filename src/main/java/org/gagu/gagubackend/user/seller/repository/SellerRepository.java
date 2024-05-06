package org.gagu.gagubackend.user.seller.repository;

import org.gagu.gagubackend.user.seller.domain.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerRepository extends JpaRepository<Seller,Long> {
    Seller findByEmail(String email);
}

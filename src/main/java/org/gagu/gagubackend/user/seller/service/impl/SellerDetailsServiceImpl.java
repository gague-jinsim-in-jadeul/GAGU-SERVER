package org.gagu.gagubackend.user.seller.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.user.seller.domain.Seller;
import org.gagu.gagubackend.user.seller.repository.SellerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SellerDetailsServiceImpl implements UserDetailsService {
    private final SellerRepository sellerRepository;
    @Override
    public UserDetails loadUserByUsername(String buyerName) throws UsernameNotFoundException {
        Seller buyer = sellerRepository.findByEmail(buyerName);
        return buyer;
    }
}

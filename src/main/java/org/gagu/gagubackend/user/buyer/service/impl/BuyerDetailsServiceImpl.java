package org.gagu.gagubackend.user.buyer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.user.buyer.domain.Buyer;
import org.gagu.gagubackend.user.buyer.repository.BuyerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuyerDetailsServiceImpl implements UserDetailsService {
    private final BuyerRepository buyerRepository;
    @Override
    public UserDetails loadUserByUsername(String buyerName) throws UsernameNotFoundException {
        Buyer buyer = buyerRepository.findByEmail(buyerName);
        return buyer;
    }
}

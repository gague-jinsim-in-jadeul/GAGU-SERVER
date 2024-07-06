package org.gagu.gagubackend.auth.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.auth.dao.NicknameDAO;
import org.gagu.gagubackend.user.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class NicknameDAOImpl implements NicknameDAO {
    private final AtomicInteger counter = new AtomicInteger(1);
    private final UserRepository userRepository;

    @Override
    public synchronized String generateNickName() {
        String nickname;

        do {
            int number = counter.getAndIncrement();
            nickname = "GAGU#" + number;
        } while(checkNickname(nickname));
        return nickname;
    }
    private boolean checkNickname(String nickname){
        return userRepository.existsByNickName(nickname);
    }
}

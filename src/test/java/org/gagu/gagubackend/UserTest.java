package org.gagu.gagubackend;

import org.gagu.gagubackend.auth.dao.AuthDAO;
import org.gagu.gagubackend.auth.dto.request.RequestAddressDto;
import org.gagu.gagubackend.auth.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserTest {
    @Autowired
    UserRepository userRepository;
    @Autowired
    AuthDAO authDAO;

    @Test
    @DisplayName("유저 위도, 경도 저장")
    public void createUser(){
        for(int i=1; i<=21; i++){
            String nickname = "test"+i;
            RequestAddressDto dto = new RequestAddressDto();
            authDAO.saveUserAddress(dto,nickname);
        }
    }
}

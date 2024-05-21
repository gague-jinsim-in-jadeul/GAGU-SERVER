package org.gagu.gagubackend.auth.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.auth.dao.AuthDAO;
import org.gagu.gagubackend.auth.dto.request.RequestAuthDto;
import org.gagu.gagubackend.auth.dto.response.ResponseAuthDto;
import org.gagu.gagubackend.global.domain.CommonResponse;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.gagu.gagubackend.global.security.JwtTokenProvider;
import org.gagu.gagubackend.user.domain.User;
import org.gagu.gagubackend.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthDAOImpl implements AuthDAO {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    @Override
    public ResponseEntity<?> login(RequestAuthDto requestAuthDto) {
        if (checkUserExist(requestAuthDto.getEmail(), requestAuthDto.getLoginType())) {
            User user = userRepository.findByEmailAndLoginType(requestAuthDto.getEmail(), requestAuthDto.getLoginType());
            log.info("user name : {}",user.getUsername());
            if (user.isEnabled()) {
                return ResponseEntity.status(ResultCode.OK.getCode())
                        .body(ResponseAuthDto.builder()
                                .accessToken(jwtTokenProvider.createAccessToken(user.getEmail(), user.getRoles()))
                                .refreshToken(jwtTokenProvider.createRefreshToken(user.getEmail()))
                                .name(user.getUsername())
                                .status(CommonResponse.success())
                                .build());

            } else {
                return ResponseEntity.status(ResultCode.DELETED_USER.getCode())
                        .body(ResultCode.DELETED_USER);
            }
        } else {
            log.info("[sign up] no user");
            CommonResponse commonResponse = signUp(requestAuthDto);
            if (commonResponse.getCode() == 200) {
                return login(requestAuthDto);
            }
        }
        return null;
    }


    private CommonResponse signUp(RequestAuthDto requestAuthDto){
        User user = User.builder()
                .name(requestAuthDto.getName())
                .nickName(requestAuthDto.getNickName())
                .password(requestAuthDto.getPassword())
                .phoneNumber(requestAuthDto.getPhoneNumber())
                .email(requestAuthDto.getEmail())
                .profileUrl(requestAuthDto.getProfileUrl())
                .loginType(requestAuthDto.getLoginType())
                .useAble(requestAuthDto.isUseAble())
                .roles(Collections.singletonList("ROLE_USER"))
                .build();

        userRepository.save(user);

        return CommonResponse.success();
    }
    private boolean checkUserExist(String email, String loginType){
        return userRepository.existsByEmailAndLoginType(email, loginType);
    }
}

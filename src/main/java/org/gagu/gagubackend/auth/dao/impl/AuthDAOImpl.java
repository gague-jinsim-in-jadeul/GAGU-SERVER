package org.gagu.gagubackend.auth.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.auth.dao.AuthDAO;
import org.gagu.gagubackend.auth.dao.NicknameDAO;
import org.gagu.gagubackend.auth.dto.request.RequestAddressDto;
import org.gagu.gagubackend.auth.dto.request.RequestGeneralSignDto;
import org.gagu.gagubackend.auth.dto.request.RequestSaveUserDto;
import org.gagu.gagubackend.auth.dto.response.ResponseAuthDto;
import org.gagu.gagubackend.auth.dto.response.ResponseProfileDto;
import org.gagu.gagubackend.global.domain.CommonResponse;
import org.gagu.gagubackend.global.domain.enums.LoginType;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.gagu.gagubackend.global.security.JwtTokenProvider;
import org.gagu.gagubackend.user.domain.User;
import org.gagu.gagubackend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthDAOImpl implements AuthDAO {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final NicknameDAO nicknameDAO;

    @Value("${login.type.kakao.logo}")
    private String kaKaoLoginLogo;
    @Value("${login.type.google.logo}")
    private String googleLoginLogo;
    @Value("${login.type.general.logo}")
    private String generalLoginLogo;
    @Override
    public ResponseEntity<?> login(RequestSaveUserDto requestSaveUserDto) {
        if (requestSaveUserDto.getLoginType().equals(LoginType.GENERAL.toString())) {
            if (checkWorkshopExist(requestSaveUserDto.getNickName(), requestSaveUserDto.getLoginType())) {
                User user = userRepository.findByEmailAndNickName(requestSaveUserDto.getEmail(), requestSaveUserDto.getNickName());
                if (user.isEnabled()) {
                    return ResponseEntity.status(ResultCode.DUPLICATE_USER.getCode())
                            .body(ResultCode.DUPLICATE_USER.getMessage());
                } else {
                    return ResponseEntity.status(ResultCode.DELETED_USER.getCode())
                            .body(ResultCode.DELETED_USER);
                }

            } else {
                log.info("[sign up] no user");
                log.warn("[sign up] save user");
                try {
                    User user = User.builder()
                            .name(requestSaveUserDto.getName())
                            .nickName(requestSaveUserDto.getNickName())
                            .password(requestSaveUserDto.getPassword())
                            .phoneNumber(requestSaveUserDto.getPhoneNumber())
                            .email(requestSaveUserDto.getEmail())
                            .profileUrl(requestSaveUserDto.getProfileUrl())
                            .loginType(requestSaveUserDto.getLoginType())
                            .profileMessage(requestSaveUserDto.getProfileMessage())
                            .useAble(requestSaveUserDto.isUseAble())
                            .roles(Collections.singletonList("ROLE_WORKSHOP"))
                            .build();
                    userRepository.save(user);
                    log.info("[sign up] save user success!");
                    return ResponseEntity.status(ResultCode.OK.getCode())
                            .body(ResponseAuthDto.builder()
                                    .accessToken(jwtTokenProvider.createAccessToken(user.getEmail(), user.getNickName(), user.getRoles()))
                                    .refreshToken(jwtTokenProvider.createRefreshToken(user.getEmail(), user.getNickName()))
                                    .name(user.getUsername())
                                    .status(CommonResponse.success())
                                    .build());
                } catch (DataIntegrityViolationException e) {
                    e.printStackTrace();
                    log.warn("[auth] signup failed due to duplicate nickname");
                    return ResponseEntity.status(ResultCode.DUPLICATE_NICKNAME.getCode()).body(ResultCode.DUPLICATE_NICKNAME.getMessage());

                }
            }
        } else {
            if (checkUserExist(requestSaveUserDto.getEmail(), requestSaveUserDto.getLoginType())) {
                User user = userRepository.findByEmailAndLoginType(requestSaveUserDto.getEmail(), requestSaveUserDto.getLoginType());
                if (user.isEnabled()) {
                    return ResponseEntity.status(ResultCode.OK.getCode())
                            .body(ResponseAuthDto.builder()
                                    .accessToken(jwtTokenProvider.createAccessToken(user.getEmail(), user.getNickName(), user.getRoles()))
                                    .refreshToken(jwtTokenProvider.createRefreshToken(user.getEmail(), user.getNickName()))
                                    .name(user.getUsername())
                                    .status(CommonResponse.success())
                                    .build());

                } else {
                    return ResponseEntity.status(ResultCode.DELETED_USER.getCode())
                            .body(ResultCode.DELETED_USER);
                }
            } else {
                log.info("[sign up] no user");
                User user = User.builder()
                        .name(requestSaveUserDto.getName())
                        .nickName(nicknameDAO.generateNickName())
                        .password(requestSaveUserDto.getPassword())
                        .phoneNumber(requestSaveUserDto.getPhoneNumber())
                        .email(requestSaveUserDto.getEmail())
                        .profileUrl(requestSaveUserDto.getProfileUrl())
                        .loginType(requestSaveUserDto.getLoginType())
                        .useAble(requestSaveUserDto.isUseAble())
                        .roles(Collections.singletonList("ROLE_USER"))
                        .build();

                int retryCount = 0;
                final int maxRetries = 5; // 재시도 횟수 제한
                while (retryCount < maxRetries) {
                    try {
                        userRepository.save(user);
                        log.info("[auth] signup success");

                        return ResponseEntity.status(ResultCode.OK.getCode())
                                .body(ResponseAuthDto.builder()
                                        .accessToken(jwtTokenProvider.createAccessToken(user.getEmail(), user.getNickName(), user.getRoles()))
                                        .refreshToken(jwtTokenProvider.createRefreshToken(user.getEmail(), user.getNickName()))
                                        .name(requestSaveUserDto.getName())
                                        .status(CommonResponse.success())
                                        .build());
                    } catch (DataIntegrityViolationException e) {
                        log.warn("[auth] signup failed due to duplicate nickname, retrying...");
                        user.setNickName(nicknameDAO.generateNickName());
                        retryCount++;
                    }
                }
            }
            return null;
        }
    }

    @Override
    public ResponseEntity<?> generalLogin(RequestGeneralSignDto requestGeneralSignDto, String type) {
        Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        List<User> userList = userRepository.findAllByEmailAndLoginType(requestGeneralSignDto.getEmail(), type);

        if(userList.size() > 1){ // 공방 관계자 중 동일한 이메일, 로그인 타입이 있을 때
            log.info("[auth] duplicate email.. check user password..");
            for(User tmp : userList){
                String password = tmp.getPassword();
                if (encoder.matches(requestGeneralSignDto.getPassword(),password)){
                    log.info("[auth] check user password success!");
                    return ResponseEntity.status(ResultCode.OK.getCode())
                            .body(ResponseAuthDto.builder()
                                    .accessToken(jwtTokenProvider.createAccessToken(tmp.getEmail(), tmp.getNickName(),tmp.getRoles()))
                                    .refreshToken(jwtTokenProvider.createRefreshToken(tmp.getEmail(), tmp.getNickName()))
                                    .name(tmp.getUsername())
                                    .status(CommonResponse.success())
                                    .build());
                }
            } // 비밀번호가 일치하지 않을 때
            log.info("[auth] no user..");
            return ResponseEntity.status(ResultCode.PASSWORD_NOT_MATCH.getCode())
                        .body(ResultCode.PASSWORD_NOT_MATCH.getMessage());

        }else{ // 중복된 공방관계자 이메일이 없을 때
            log.info("[auth] no duplicate email! check user password");
            User user = userList.get(0);
            String password = user.getPassword();

            if(encoder.matches(requestGeneralSignDto.getPassword(), password)){
                return ResponseEntity.status(ResultCode.OK.getCode())
                        .body(ResponseAuthDto.builder()
                                .accessToken(jwtTokenProvider.createAccessToken(user.getEmail(), user.getNickName(),user.getRoles()))
                                .refreshToken(jwtTokenProvider.createRefreshToken(user.getEmail(), user.getNickName()))
                                .name(user.getUsername())
                                .status(CommonResponse.success())
                                .build());
            }else{
                return ResponseEntity.status(ResultCode.PASSWORD_NOT_MATCH.getCode())
                        .body(ResultCode.PASSWORD_NOT_MATCH.getMessage());
            }
        }
    }

    @Override
    public ResponseEntity<?> changeUserProfile(String nickname, String fileUrl) {
        log.info("[change profile] file url : {}", fileUrl);
        User user = userRepository.findByNickName(nickname);
        if(!(user == null)){
            String profileUrl = user.getProfileUrl();
            if(profileUrl.equals(fileUrl)){
                return ResponseEntity.status(ResultCode.DUPLICATE_PROFILE.getCode()).body(ResultCode.DUPLICATE_PROFILE.getMessage());
            }else{

                user.setProfileUrl(fileUrl);
                userRepository.save(user);
                return ResponseEntity.ok("정상적으로 프로필이 변경되었습니다.");
            }
        }
        return ResponseEntity.status(ResultCode.NOT_FOUND_USER.getCode()).body(ResultCode.NOT_FOUND_USER.getMessage());
    }

    @Override
    public ResponseEntity<?> checkUserProfile(String nickname) {
        log.info("[auth] check user by nickname");
        User user = userRepository.findByNickName(nickname);

        if(user == null){
            return ResponseEntity.status(ResultCode.NOT_FOUND_USER.getCode()).body(ResultCode.NOT_FOUND_USER.getMessage());
        }else if (!user.isEnabled()){
            return ResponseEntity.status(ResultCode.DELETED_USER.getCode()).body(ResultCode.DELETED_USER.getMessage());
        }else{
            ResponseProfileDto responseProfileDto = new ResponseProfileDto();
            responseProfileDto.setProfileUrl(user.getProfileUrl());
            responseProfileDto.setName(user.getName());
            responseProfileDto.setEmail(user.getEmail());
            responseProfileDto.setAddress(user.getAddress());
            switch (user.getLoginType()){
                case "GOOGLE":
                    responseProfileDto.setLoginTypeLogo(googleLoginLogo);
                    return ResponseEntity.ok(responseProfileDto);
                case "KAKAO":
                    responseProfileDto.setLoginTypeLogo(kaKaoLoginLogo);
                    return ResponseEntity.ok(responseProfileDto);
                case "GENERAL":
                    responseProfileDto.setLoginTypeLogo(generalLoginLogo);
                    return ResponseEntity.ok(responseProfileDto);
            }
        }
        return null;
    }

    @Override
    public ResponseEntity<?> saveUserAddress(RequestAddressDto requestAddressDto, String nickname) {
        log.info("[auth] saving {}'s address...",nickname);
        User user = userRepository.findByNickName(nickname);
        if(user == null){
            return ResultCode.NOT_FOUND_USER.toResponseEntity();
        }else{
            try{
                user.setAddress(requestAddressDto.getAddress());
                userRepository.save(user);
                log.info("[auth] successfully save address!");
                return ResultCode.OK.toResponseEntity();
            }catch (Exception e){
                log.error("[auth] fail to save address!");
                e.printStackTrace();
                return ResultCode.FAIL.toResponseEntity();
            }
        }
    }

    private boolean checkUserExist(String email, String loginType){
        return userRepository.existsByEmailAndLoginType(email, loginType);
    }
    private boolean checkWorkshopExist(String nickname, String loginType){
        return userRepository.existsByNickNameAndLoginType(nickname,loginType);
    }
}

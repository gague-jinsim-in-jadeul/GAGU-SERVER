package org.gagu.gagubackend.auth.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.auth.dao.AuthDAO;
import org.gagu.gagubackend.auth.dao.NicknameDAO;
import org.gagu.gagubackend.auth.dto.request.RequestGeneralSignDto;
import org.gagu.gagubackend.auth.dto.request.RequestSaveUserDto;
import org.gagu.gagubackend.auth.dto.response.ResponseAuthDto;
import org.gagu.gagubackend.global.domain.CommonResponse;
import org.gagu.gagubackend.global.domain.enums.LoginType;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.gagu.gagubackend.global.security.JwtTokenProvider;
import org.gagu.gagubackend.user.domain.User;
import org.gagu.gagubackend.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthDAOImpl implements AuthDAO {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final NicknameDAO nicknameDAO;
    @Override
    public ResponseEntity<?> login(RequestSaveUserDto requestSaveUserDto) {
        if(requestSaveUserDto.getLoginType().equals(LoginType.GENERAL.toString())){
            if(checkWorkshopExist(requestSaveUserDto.getNickName(), requestSaveUserDto.getLoginType())){
                User user = userRepository.findByEmailAndNickName(requestSaveUserDto.getEmail(), requestSaveUserDto.getNickName());
                if(user.isEnabled()){
                    return ResponseEntity.status(ResultCode.OK.getCode())
                            .body(ResponseAuthDto.builder()
                                    .accessToken(jwtTokenProvider.createAccessToken(user.getEmail(), user.getNickName(),user.getRoles()))
                                    .refreshToken(jwtTokenProvider.createRefreshToken(user.getEmail(), user.getNickName()))
                                    .name(user.getUsername())
                                    .status(CommonResponse.success())
                                    .build());
                }else{
                    return ResponseEntity.status(ResultCode.DELETED_USER.getCode())
                            .body(ResultCode.DELETED_USER);
                }

            }else{
                log.info("[sign up] no user");
                CommonResponse commonResponse = generalSignUp(requestSaveUserDto);
                if(commonResponse.getCode()==200){
                    return login(requestSaveUserDto);
                }
            }
        }else{
            if (checkUserExist(requestSaveUserDto.getEmail(), requestSaveUserDto.getLoginType())) {
                User user = userRepository.findByEmailAndLoginType(requestSaveUserDto.getEmail(), requestSaveUserDto.getLoginType());
                if (user.isEnabled()) {
                    return ResponseEntity.status(ResultCode.OK.getCode())
                            .body(ResponseAuthDto.builder()
                                    .accessToken(jwtTokenProvider.createAccessToken(user.getEmail(), user.getNickName(),user.getRoles()))
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
                CommonResponse commonResponse = signUp(requestSaveUserDto);
                if (commonResponse.getCode() == 200) {
                    return login(requestSaveUserDto);
                }
            }
        }
        return null;
    }

    @Override
    public ResponseEntity<?> generalLogin(RequestGeneralSignDto requestGeneralSignDto, String type) {
        User user = userRepository.findByEmailAndLoginType(requestGeneralSignDto.getEmail(), type);

        String password = user.getPassword();
        Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

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

    private CommonResponse signUp(RequestSaveUserDto requestSaveUserDto){
        log.info("[auth] sign up");

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
        try{
            userRepository.save(user);
            log.info("[auth] signup success");
        }catch (DataIntegrityViolationException e){
            log.warn("[auth] signup failed due to duplicate nickname, retrying...");
            user.setNickName(nicknameDAO.generateNickName());
            userRepository.save(user);
        }

        return CommonResponse.success();
    }

    private CommonResponse generalSignUp(RequestSaveUserDto requestSaveUserDto){
        log.info("[auth] sign up");

        User user = User.builder()
                .name(requestSaveUserDto.getName())
                .nickName(requestSaveUserDto.getNickName())
                .password(requestSaveUserDto.getPassword())
                .phoneNumber(requestSaveUserDto.getPhoneNumber())
                .email(requestSaveUserDto.getEmail())
                .profileUrl(requestSaveUserDto.getProfileUrl())
                .loginType(requestSaveUserDto.getLoginType())
                .useAble(requestSaveUserDto.isUseAble())
                .roles(Collections.singletonList("ROLE_WORKSHOP"))
                .build();
        try{
            userRepository.save(user);
            log.info("[auth] signup success");
            return CommonResponse.success();
        }catch (DataIntegrityViolationException e){
            log.warn("[auth] signup failed due to duplicate nickname, retrying...");
            return CommonResponse.fail(ResultCode.FAIL);
        }
    }
    private boolean checkUserExist(String email, String loginType){
        return userRepository.existsByEmailAndLoginType(email, loginType);
    }
    private boolean checkWorkshopExist(String nickname, String loginType){
        return userRepository.existsByNickNameAndLoginType(nickname,loginType);
    }
}

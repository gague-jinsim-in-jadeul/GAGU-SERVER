package org.gagu.gagubackend.auth.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.auth.dao.AuthDAO;
import org.gagu.gagubackend.auth.dao.NicknameDAO;
import org.gagu.gagubackend.auth.domain.StarReview;
import org.gagu.gagubackend.auth.dto.request.RequestAddressDto;
import org.gagu.gagubackend.auth.dto.request.RequestChangeUserInfoDto;
import org.gagu.gagubackend.auth.dto.request.RequestGeneralSignDto;
import org.gagu.gagubackend.auth.dto.request.RequestSaveUserDto;
import org.gagu.gagubackend.auth.dto.response.ResponseAuthDto;
import org.gagu.gagubackend.auth.dto.response.ResponseProfileDto;
import org.gagu.gagubackend.auth.dto.response.ResponseWorkShopDetailsDto;
import org.gagu.gagubackend.auth.repository.StarReviewRepository;
import org.gagu.gagubackend.chat.domain.ChatRoom;
import org.gagu.gagubackend.chat.repository.ChatRoomRepository;
import org.gagu.gagubackend.estimate.domain.Estimate;
import org.gagu.gagubackend.estimate.repository.EstimateRepository;
import org.gagu.gagubackend.global.config.RedisConfig;
import org.gagu.gagubackend.global.domain.CommonResponse;
import org.gagu.gagubackend.global.domain.enums.LoginType;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.gagu.gagubackend.global.security.JwtTokenProvider;
import org.gagu.gagubackend.auth.domain.User;
import org.gagu.gagubackend.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthDAOImpl implements AuthDAO {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final NicknameDAO nicknameDAO;
    private final RedisConfig redisConfig;
    private final ChatRoomRepository chatRoomRepository;
    private final StarReviewRepository starReviewRepository;
    private final EstimateRepository estimateRepository;

    @Value("${login.type.kakao.logo}")
    private String kaKaoLoginLogo;
    @Value("${login.type.google.logo}")
    private String googleLoginLogo;
    @Value("${login.type.general.logo}")
    private String generalLoginLogo;
    @Override
    public ResponseEntity<?> generalLogin(RequestSaveUserDto requestSaveUserDto) {
        if (requestSaveUserDto.getLoginType().equals(LoginType.GENERAL.toString())) { // 공방관계자 회원가입 일 경우
            if (checkWorkshopExist(requestSaveUserDto.getNickName(), requestSaveUserDto.getLoginType())) { // 이미 공방관계자 계정이 있는 경우
                log.info("[auth] user is exist! user : {}", requestSaveUserDto.getNickName());

                User user = userRepository.findByNickName(requestSaveUserDto.getNickName()); // 공방 관계자
                if (user.isEnabled()) {
                    log.error("[auth] nick name is duplicated!");
                    return ResultCode.DUPLICATE_NICKNAME.toResponseEntity();
                } else {
                    return ResultCode.DELETED_USER.toResponseEntity();
                }

            } else { // 새 유저
                log.info("[sign up] new user!");
                log.warn("[sign up] saving user...");
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
                            .FCMToken(requestSaveUserDto.getFCMToken())
                            .useAble(requestSaveUserDto.isUseAble())
                            .roles(Collections.singletonList("ROLE_WORKSHOP"))
                            .build();
                    userRepository.save(user);

                    log.info("[auth] saving review count table..");

                    StarReview starReview = StarReview.builder()
                            .workshopName(requestSaveUserDto.getNickName())
                            .starsAverage(BigDecimal.valueOf(0.0))
                            .sum(new BigDecimal(0))
                            .count(BigInteger.valueOf(0))
                            .workshop(user)
                            .build();

                    starReviewRepository.save(starReview);

                    log.info("[auth] save review count table success!");

                    log.info("[sign up] save user success!");

                    String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getNickName());
                    log.info("[auth] refresh token : {}", refreshToken);

                    redisConfig.redisTemplate().opsForValue().set(user.getNickName(),
                            refreshToken,
                            jwtTokenProvider.getExpireTime(refreshToken).getTime() - System.currentTimeMillis(),
                            TimeUnit.MILLISECONDS
                    ); // put {nickname : token} redis

                    log.info("[auth] put token to redis success!");

                    return ResponseEntity.status(ResultCode.OK.getCode())
                            .body(ResponseAuthDto.builder()
                                    .accessToken(jwtTokenProvider.createAccessToken(user.getEmail(), user.getNickName(), user.getRoles()))
                                    .nickname(user.getNickName())
                                    .name(user.getName())
                                    .status(CommonResponse.success())
                                    .build());
                } catch (DataIntegrityViolationException e) {
                    e.printStackTrace();
                    log.error("[auth] fail to save user");
                    return ResultCode.BAD_REQUEST.toResponseEntity();
                }
            }
        } else { // 소셜 로그인일 경우
            if (checkUserExist(requestSaveUserDto.getEmail(), requestSaveUserDto.getLoginType())) { // 소셜 로그인 구매자 조회
                log.info("[social auth] user is exist!");
                User user = userRepository.findByEmailAndLoginType(requestSaveUserDto.getEmail(), requestSaveUserDto.getLoginType());
                if (user.isEnabled()) {
                    log.info("[social auth] user is available");

                    String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getNickName());

                    redisConfig.redisTemplate().opsForValue().set(user.getNickName(),
                            refreshToken,
                            jwtTokenProvider.getExpireTime(refreshToken).getTime() - System.currentTimeMillis(),
                            TimeUnit.MILLISECONDS
                    ); // put {nickname : token} redis

                    log.info("[workshop auth] update user fcm token!");
                    user.setFCMToken(requestSaveUserDto.getFCMToken());
                    userRepository.save(user); // user 로그인 시 FCM 토큰 업데이트

                    log.info("[social auth] put token to redis success!");

                    log.info("[social auth] success to social login!");

                    return ResponseEntity.status(ResultCode.OK.getCode())
                            .body(ResponseAuthDto.builder()
                                    .accessToken(jwtTokenProvider.createAccessToken(user.getEmail(), user.getNickName(), user.getRoles()))
                                    .nickname(user.getNickName())
                                    .name(user.getName())
                                    .status(CommonResponse.success())
                                    .build());

                } else {
                    return ResultCode.DELETED_USER.toResponseEntity();
                }
            } else { // 새로운 유저
                log.info("[social auth] new user!");

                String nickname = nicknameDAO.generateNickName();

                if (!(userRepository.existsByNickName(nickname))) {
                    log.info("[social auth] nickname : {}", nickname);

                    User user = User.builder()
                            .name(requestSaveUserDto.getName())
                            .nickName(nickname)
                            .password(requestSaveUserDto.getPassword())
                            .phoneNumber(requestSaveUserDto.getPhoneNumber())
                            .email(requestSaveUserDto.getEmail())
                            .profileUrl(requestSaveUserDto.getProfileUrl())
                            .loginType(requestSaveUserDto.getLoginType())
                            .FCMToken(requestSaveUserDto.getFCMToken())
                            .useAble(requestSaveUserDto.isUseAble())
                            .roles(Collections.singletonList("ROLE_USER"))
                            .build();

                    userRepository.save(user);

                    String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getNickName());

                    log.info("[social auth] refresh token : {}", refreshToken);

                    redisConfig.redisTemplate().opsForValue().set(user.getNickName(),
                            refreshToken,
                            jwtTokenProvider.getExpireTime(refreshToken).getTime() - System.currentTimeMillis(),
                            TimeUnit.MILLISECONDS
                    ); // put {nickname : token} redis

                    log.info("[social auth] put token to redis success!");

                    return ResponseEntity.status(ResultCode.OK.getCode())
                            .body(ResponseAuthDto.builder()
                                    .accessToken(jwtTokenProvider.createAccessToken(user.getEmail(), user.getNickName(), user.getRoles()))
                                    .nickname(user.getNickName())
                                    .name(user.getName())
                                    .status(CommonResponse.success())
                                    .build());
                }
                return ResponseEntity.status(ResultCode.FAIL.getCode()).body("알 수 없는 원인 때문에 회원가입에 실패하였습니다. 관리자에게 문의해주세요.");
            }
        }
    }

    @Override
    public ResponseEntity<?> workshopLogin(RequestGeneralSignDto requestGeneralSignDto, String type) {
        Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        List<User> userList = userRepository.findAllByEmailAndLoginType(requestGeneralSignDto.getEmail(), type);

        if(userList.size() > 1){ // 공방 관계자 중 동일한 이메일, 로그인 타입이 있을 때
            log.info("[workshop auth] duplicate email.. check user password..");
            for(User tmp : userList){
                String password = tmp.getPassword();
                if (encoder.matches(requestGeneralSignDto.getPassword(),password)){ // 패스워드, 이메일 일치하는 계정 찾았을 때
                    log.info("[workshop auth] check user password success!");

                    log.info("[workshop auth] update user fcm token!");
                    tmp.setFCMToken(requestGeneralSignDto.getFCMToken());
                    userRepository.save(tmp);

                    String refreshToken = jwtTokenProvider.createRefreshToken(tmp.getEmail(), tmp.getNickName());

                    redisConfig.redisTemplate().opsForValue().set(tmp.getNickName(),
                            refreshToken,
                            jwtTokenProvider.getExpireTime(refreshToken).getTime() - System.currentTimeMillis(),
                            TimeUnit.MILLISECONDS
                    ); // put {nickname : token} redis

                    log.info("[workshop auth] put token to redis success!");

                    return ResponseEntity.status(ResultCode.OK.getCode())
                            .body(ResponseAuthDto.builder()
                                    .accessToken(jwtTokenProvider.createAccessToken(tmp.getEmail(), tmp.getNickName(),tmp.getRoles()))
                                    .nickname(tmp.getNickName())
                                    .name(tmp.getName())
                                    .status(CommonResponse.success())
                                    .build());
                }
            } // 비밀번호가 일치하지 않을 때
            log.error("[workshop auth] password is unmatched");
            return ResultCode.PASSWORD_NOT_MATCH.toResponseEntity();

        }else{ // 공방관계자 계정이 유일 할 때
            log.info("[workshop auth] no duplicate email! check user password");
            User user = userList.get(0);
            String password = user.getPassword();

            if(encoder.matches(requestGeneralSignDto.getPassword(), password)){
                log.info("[workshop auth] check user password success!");

                log.info("[workshop auth] update user fcm token!");
                user.setFCMToken(requestGeneralSignDto.getFCMToken());
                userRepository.save(user);

                String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getNickName());

                log.info("[workshop auth] refresh token : {}", refreshToken);

                redisConfig.redisTemplate().opsForValue().set(user.getNickName(),
                        refreshToken,
                        jwtTokenProvider.getExpireTime(refreshToken).getTime() - System.currentTimeMillis(),
                        TimeUnit.MILLISECONDS
                ); // put {nickname : token} redis

                log.info("[workshop auth] put token to redis success!");

                return ResponseEntity.status(ResultCode.OK.getCode())
                        .body(ResponseAuthDto.builder()
                                .accessToken(jwtTokenProvider.createAccessToken(user.getEmail(), user.getNickName(),user.getRoles()))
                                .nickname(user.getNickName())
                                .name(user.getName())
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
            responseProfileDto.setNickname(nickname);
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

    @Override
    public ResponseEntity<?> deleteToken(String token){
        String nickName = jwtTokenProvider.getUserNickName(token);
        log.info("[auth] logout user name : {}", nickName);

        if (redisConfig.redisTemplate().opsForValue().get(nickName)!=null){ // refresh token 이 있을 경우

            redisConfig.redisTemplate().delete(nickName); // refresh token 삭제

            redisConfig.redisTemplate().opsForValue().set(token, // save {token : logout}
                    "logout",
                    jwtTokenProvider.getExpireTime(token).getTime() - System.currentTimeMillis(),
                    TimeUnit.MILLISECONDS);

            return ResultCode.OK.toResponseEntity();
        }
        return ResultCode.NOT_IN_STORAGE.toResponseEntity();
    }

    @Override
    public ResponseEntity<?> saveUserInfo(RequestChangeUserInfoDto requestChangeUserInfoDto, String nickname) {
        log.info("[auth] checking user....");
        User user = userRepository.findByNickName(nickname);
        if(user == null){
            log.error("[auth] not found user!");
            return ResultCode.NOT_FOUND_USER.toResponseEntity();
        }else{
            if(!(user.isEnabled())){
                log.error("[auth] deleted user!");
                return ResultCode.DELETED_USER.toResponseEntity();
            }else{
                try{
                    log.info("[auth] user is founded! checking user profile..");
                    log.info("[auth] changing user info..");
                    user.setEmail(requestChangeUserInfoDto.getEmail());
                    user.setAddress(requestChangeUserInfoDto.getAddress());
                    user.setNickName(requestChangeUserInfoDto.getNickname());

                    log.info("[auth] user role : {}", user.getRoles());

                    userRepository.save(user);

                    if(user.getRoles().get(0).equals("ROLE_WORKSHOP")){
                        updateWorkshopName(nickname, requestChangeUserInfoDto.getNickname());
                    }else{
                        updateUserNickName(nickname, requestChangeUserInfoDto.getNickname());
                    }

                    log.info("[auth] update user info successfully!");
                    return ResultCode.OK.toResponseEntity();
                }catch (Exception e){
                    e.printStackTrace();
                    return ResultCode.FAIL.toResponseEntity();
                }
            }
        }
    }

    @Override
    public ResponseEntity<?> getWorkShopDetails(Long id) {
        User user = userRepository.findById(id).get();
        if(user == null){
            log.info("[WORKSHOP-DETAILS] no user!");
            return ResultCode.NOT_FOUND_USER.toResponseEntity();
        }else{
            ResponseWorkShopDetailsDto dto = ResponseWorkShopDetailsDto.builder()
                    .workshopName(user.getNickName())
                    .description(user.getProfileMessage())
                    .address(user.getAddress())
                    .build();
            log.info("[WORKSHOP-DETAILS] found workshop successfully!");
            return ResponseEntity.ok(dto);
        }
    }

    private void updateUserNickName(String nickname, String changeNickname){
        log.info("[auth] update user nickname....");
        List<ChatRoom> chatLst = chatRoomRepository.findAllByRoomNameContains(nickname);
        chatLst.stream().map(v ->{
            String roomName = v.getRoomName();
            roomName= roomName.replace(nickname, changeNickname);
            v.setRoomName(roomName);
            chatRoomRepository.save(v);
            return null;
        }).collect(Collectors.toList());
    }

    private void updateWorkshopName(String nickname, String changeNickname){

        log.info("[auth] update chat room workshop name....");
        List<ChatRoom> chatLst = chatRoomRepository.findAllByRoomNameContains(nickname);
        chatLst.stream().map(v ->{
            String roomName = v.getRoomName();
            roomName= roomName.replace(nickname, changeNickname);
            v.setRoomName(roomName);
            chatRoomRepository.save(v);
            return null;
        }).collect(Collectors.toList());

        log.info("[auth] update estimate workshop name....");
        List<Estimate> estimateList = estimateRepository.findAllByMakerNameContains(nickname);
        estimateList.stream().map(v ->{
            String makerName = v.getMakerName();
            makerName = makerName.replace(nickname, changeNickname);
            v.setMakerName(makerName);
            estimateRepository.save(v);
            return null;
        }).collect(Collectors.toList());

        log.info("[auth] update start review workshop name....");

        List<StarReview> starReviewList = starReviewRepository.findAllByWorkshopNameContains(nickname);
        starReviewList.stream().map(v ->{
            String makerName = v.getWorkshopName();
            makerName = makerName.replace(nickname, changeNickname);
            v.setWorkshopName(makerName);
            starReviewRepository.save(v);
            return null;
        }).collect(Collectors.toList());
        log.info("[auth] update success!");
    }

    private boolean checkUserExist(String email, String loginType){
        return userRepository.existsByEmailAndLoginType(email, loginType);
    }
    private boolean checkWorkshopExist(String nickname, String loginType){
        return userRepository.existsByNickNameAndLoginType(nickname,loginType);
    }
}

package org.gagu.gagubackend.auth.dao.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.gagu.gagubackend.global.security.JwtTokenProvider;
import org.gagu.gagubackend.auth.domain.User;
import org.gagu.gagubackend.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    @Value("${kako.api.token}")
    public String KAKAO_API_TOKEN;
    @Value("${kakao.local.url}")
    public String KAKAO_LOCAL_URL;

    @Override
    public ResponseEntity<?> generalLogin(RequestSaveUserDto requestSaveUserDto) {
        String logIntType = requestSaveUserDto.getLoginType();
        String email = requestSaveUserDto.getEmail();
        String nickName = requestSaveUserDto.getNickName();

        switch(logIntType){
            case "GENERAL":
                Optional<User> optionalWorkshop= userRepository.checkWorkshopExist(nickName,logIntType);
                if (optionalWorkshop.isPresent()) { // 이미 공방관계자 계정이 있는 경우
                    log.info("[GENERAL-LOGIN] workshop is exist! user : {}", nickName);

                    User user = optionalWorkshop.get();
                    if (user.isEnabled()) {
                        log.error("[GENERAL-LOGIN] workshop name is duplicated!");
                        return ResultCode.DUPLICATE_NICKNAME.toResponseEntity();
                    } else {
                        return ResultCode.DELETED_USER.toResponseEntity();
                    }

                } else {
                    /**
                     * 새 공방 추가
                     */
                    log.info("[GENERAL-LOGIN] new workshop!");
                    log.warn("[GENERAL-LOGIN] saving workshop...");
                    try {
                        User user = new User(requestSaveUserDto, "ROLE_WORKSHOP");
                        userRepository.save(user);

                        log.info("[GENERAL-LOGIN] saving review count table..");

                        starReviewRepository.save(new StarReview(requestSaveUserDto, user));

                        log.info("[GENERAL-LOGIN] save review count table success!");
                        log.info("[GENERAL-LOGIN] save user success!");

                        String refreshToken = jwtTokenProvider.createRefreshToken(email, nickName);

                        try{
                            redisConfig.putRefreshToken(nickName,refreshToken); // put {nickname : token} redis
                            log.info("[GENERAL-LOGIN-SOCIAL] put token to redis success!");
                        }catch (Exception e){
                            log.error("[GENERAL-LOGIN-SOCIAL] redis server has wrong");
                            e.printStackTrace();
                        }

                        log.info("[GENERAL-LOGIN] put token to redis success!");

                        return ResponseEntity.ok().body(new ResponseAuthDto(user,
                                jwtTokenProvider.createAccessToken(user.getEmail(),user.getNickName(), user.getRoles())));

                    } catch (DataIntegrityViolationException e) {
                        e.printStackTrace();
                        log.error("[GENERAL-LOGIN] fail to save user");
                        return ResultCode.BAD_REQUEST.toResponseEntity();
                    }
                }
            default:
                String resourceId = requestSaveUserDto.getResourceId();
                log.info("[GENERAL-LOGIN-SOCIAL] resource id : {}", resourceId);
                Optional<User> optionalUser = userRepository.checkSocialUserExist(resourceId, logIntType);
                if (optionalUser.isPresent()) { // 소셜 로그인 구매자 조회
                    log.info("[GENERAL-LOGIN-SOCIAL] user is exist!");
                    User user = optionalUser.get();
                    if (user.isEnabled()) {
                        log.info("[GENERAL-LOGIN-SOCIAL] user is available");

                        /**
                         * 주기적인 업데이트가 필요한 email, fcm token 정보 업데이트
                         */
                        user.regularUpdate(email, resourceId, requestSaveUserDto.getFCMToken());
                        log.info("[GENERAL-LOGIN-SOCIAL] update regular user data complete!");
                        userRepository.save(user);

                        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getNickName());
                        try{
                            redisConfig.putRefreshToken(user.getNickName(), refreshToken); // put {nickname : token} redis
                            log.info("[GENERAL-LOGIN-SOCIAL] put token to redis success!");
                        }catch (Exception e){
                            log.error("[GENERAL-LOGIN-SOCIAL] redis server has wrong");
                            e.printStackTrace();
                        }

                        log.info("[GENERAL-LOGIN-SOCIAL] success to social login!");

                        return ResponseEntity.ok().body(new ResponseAuthDto(user,
                                jwtTokenProvider.createAccessToken(user.getEmail(),user.getNickName(), user.getRoles())));
                    } else {
                        return ResultCode.DELETED_USER.toResponseEntity();
                    }
                } else { // 새로운 유저
                    log.info("[GENERAL-LOGIN-SOCIAL] new user!");
                    String newNickname = nicknameDAO.generateNickName();

                    if (!(userRepository.existsByNickName(newNickname))) {
                        log.info("[GENERAL-LOGIN-SOCIAL] nickname : {}", newNickname);

                        User user = new User(requestSaveUserDto, newNickname,"ROLE_USER");
                        userRepository.save(user);

                        String refreshToken = jwtTokenProvider.createRefreshToken(email, newNickname);
                        redisConfig.putRefreshToken(newNickname, refreshToken); // put {nickname : token} redis
                        log.info("[GENERAL-LOGIN-SOCIAL] put token to redis success!");

                        return ResponseEntity.ok().body(new ResponseAuthDto(user,
                                jwtTokenProvider.createAccessToken(user.getEmail(),user.getNickName(), user.getRoles())));
                    }
                    return ResponseEntity.status(ResultCode.FAIL.getCode()).body("알 수 없는 원인 때문에 회원가입에 실패하였습니다. 관리자에게 문의해주세요.");
                }
        }
    }

    @Override
    public ResponseEntity<?> workshopLogin(RequestGeneralSignDto requestGeneralSignDto, String type) {
        Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

        Optional<List<User>> optionalUsers = userRepository.findWorkshops(requestGeneralSignDto.getEmail(), type);
        log.info("[WORKSHOP-LOGIN] user email : {}, type : {}",requestGeneralSignDto.getEmail(), type);

        if(optionalUsers.isEmpty()){
            return ResultCode.NOT_FOUND_USER.toResponseEntity();
        }else{
            List<User> userList = optionalUsers.get();
            if(userList.size() == 0){
                log.info("[WORKSHOP-LOGIN] no user!");
                return ResultCode.NOT_FOUND_USER.toResponseEntity();
            }

            if(userList.size() > 1){ // 공방 관계자 중 동일한 이메일, 로그인 타입이 있을 때
                log.info("[WORKSHOP-LOGIN] duplicate email.. check user password..");

                for(User user : userList){
                    String password = user.getPassword();
                    if (encoder.matches(requestGeneralSignDto.getPassword(), password)){ // 패스워드, 이메일 일치하는 계정 찾았을 때
                        log.info("[WORKSHOP-LOGIN] check user password success!");
                        String email = user.getEmail();
                        String nickname = user.getNickName();

                        user.workshopFCMUpdate(requestGeneralSignDto.getFCMToken());
                        userRepository.save(user);
                        log.info("[WORKSHOP-LOGIN] update user fcm token!");

                        String refreshToken = jwtTokenProvider.createRefreshToken(email, nickname);

                        redisConfig.putRefreshToken(nickname, refreshToken); // put {nickname : token} redis; // put {nickname : token} redis

                        log.info("[WORKSHOP-LOGIN] put token to redis success!");

                        return ResponseEntity.ok()
                                .body(new ResponseAuthDto(user,
                                        jwtTokenProvider.createAccessToken(user.getEmail(),user.getNickName(), user.getRoles())));
                    }
                } // 비밀번호가 일치하지 않을 때
                log.error("[WORKSHOP-LOGIN] password is unmatched");
                return ResultCode.PASSWORD_NOT_MATCH.toResponseEntity();

            }else{ // 공방관계자 계정이 유일 할 때
                log.info("[WORKSHOP-LOGIN] no duplicate email! check user password");
                log.info("[WORKSHOP-LOGIN] userList: {}", userList);
                User user = userList.get(0);
                String password = user.getPassword();
                String nickname = user.getNickName();

                if(!encoder.matches(requestGeneralSignDto.getPassword(), password)) {
                    return ResponseEntity.status(ResultCode.PASSWORD_NOT_MATCH.getCode())
                            .body(ResultCode.PASSWORD_NOT_MATCH.getMessage());
                }
                    log.info("[WORKSHOP-LOGIN] check user password success!");

                    user.workshopFCMUpdate(requestGeneralSignDto.getFCMToken());
                    userRepository.save(user);
                    log.info("[WORKSHOP-LOGIN] update user fcm token!");

                    String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getNickName());

                    redisConfig.putRefreshToken(nickname, refreshToken); // put {nickname : token} redis;

                    log.info("[WORKSHOP-LOGIN] put token to redis success!");

                    return ResponseEntity.ok()
                            .body(new ResponseAuthDto(user,
                                    jwtTokenProvider.createAccessToken(user.getEmail(),user.getNickName(), user.getRoles())));
                }
            }
        }

    @Override
    public ResponseEntity<?> changeUserProfile(String nickname, String fileUrl) {
        log.info("[CHANGE-USER-PROFILE] file url : {}", fileUrl);
        Optional<User> optionalUser = userRepository.findUserByNickname(nickname);

        checkUserData(optionalUser);

        User user = optionalUser.get();

        String profileUrl = user.getProfileUrl();
        if(profileUrl.equals(fileUrl)){
            return ResultCode.DUPLICATE_PROFILE.toResponseEntity();
        }else{
            user.profileUpdate(fileUrl);
            try{
                userRepository.save(user);
            }catch (DataIntegrityViolationException e){
                log.error("[CHANGE-USER-PROFILE] Data integrity violation: {}", e.getMessage());
                return ResponseEntity.badRequest().body("프로필 URL의 길이가 너무 깁니다.");
            }
            return ResponseEntity.ok("정상적으로 프로필이 변경되었습니다.");
        }
    }

    @Override
    public ResponseEntity<?> checkUserProfile(String nickname) {
        log.info("[CHECK-USER-PROFILE] check user by nickname");
        Optional<User> optionalUser = userRepository.findUserByNickname(nickname);

        checkUserData(optionalUser);

        User user = optionalUser.get();
        ResponseProfileDto responseProfileDto = new ResponseProfileDto(user);

        switch (user.getLoginType()){
            case "GOOGLE":
                responseProfileDto.setLoginTypeLogo(googleLoginLogo);
                return ResponseEntity.ok(responseProfileDto);
            case "KAKAO":
                responseProfileDto.setLoginTypeLogo(kaKaoLoginLogo);
                return ResponseEntity.ok(responseProfileDto);
            default:
                responseProfileDto.setLoginTypeLogo(generalLoginLogo);
                return ResponseEntity.ok(responseProfileDto);
        }
    }

    @Override
    public ResponseEntity<?> saveUserAddress(RequestAddressDto requestAddressDto, String nickname) {
        log.info("[SAVE-USER-ADDRESS] saving {}'s address...",nickname);
        Optional<User> optionalUser = userRepository.findUserByNickname(nickname);

        checkUserData(optionalUser);

        try{
            User user = optionalUser.get();
            String address = user.getAddress();

            double[] coordinate = changeToCoordinate(address);
            user.addressUpdate(user.getAddress(), coordinate);

            userRepository.save(user);
            log.info("[SAVE-USER-ADDRESS] successfully save address!");
            return ResultCode.OK.toResponseEntity();
        }catch (Exception e){
            log.error("[SAVE-USER-ADDRESS] fail to save address!");
            e.printStackTrace();
            return ResultCode.FAIL.toResponseEntity();
        }
    }


    @Override
    public ResponseEntity<?> deleteToken(String token){
        String nickName = jwtTokenProvider.getUserNickName(token);
        log.info("[DELETE-TOKEN] logout user name : {}", nickName);

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
        String changeAddress = requestChangeUserInfoDto.getAddress();
        String changeNickname = requestChangeUserInfoDto.getNickname();
        log.info("[SAVE-USER-INFO] checking user....");
        Optional<User> optionalUser = userRepository.findUserByNickname(nickname);

        checkUserData(optionalUser);
        User user = optionalUser.get();

        if (user.getNickName().equals(changeNickname) && user.getAddress().equals(changeAddress)) {
            return ResultCode.OK.toResponseEntity();
        } else {
            if (user.getNickName().equals(changeNickname)) {
                try {
                    double[] coordinate = changeToCoordinate(changeAddress);
                    user.addressUpdate(changeAddress, coordinate);
                    userRepository.save(user);
                } catch (Exception e) {
                    log.error("[SAVE-USER-INFO] fail to change address!");
                    e.printStackTrace();
                    return ResultCode.FAIL.toResponseEntity();
                }
            } else if (user.getAddress().equals(changeAddress)) {
                try {
                    user.updateNickname(changeNickname);
                    userRepository.save(user);
                    if (user.getRoles().get(0).equals("ROLE_WORKSHOP")) {
                        updateWorkshopName(nickname, requestChangeUserInfoDto.getNickname());
                    } else {
                        updateUserNickName(nickname, requestChangeUserInfoDto.getNickname());
                    }
                    return ResultCode.OK.toResponseEntity();
                } catch (Exception e) {
                    log.error("[SAVE-USER-INFO] fail to change nickname!");
                    e.printStackTrace();
                    return ResultCode.FAIL.toResponseEntity();
                }
            } else {
                try {
                    double[] coordinate = changeToCoordinate(changeAddress);
                    user.addressUpdate(changeAddress, coordinate);
                    user.updateNickname(changeNickname);
                    userRepository.save(user);

                    if (user.getRoles().get(0).equals("ROLE_WORKSHOP")) {
                        updateWorkshopName(nickname, requestChangeUserInfoDto.getNickname());
                    } else {
                        updateUserNickName(nickname, requestChangeUserInfoDto.getNickname());
                    }
                    return ResultCode.OK.toResponseEntity();
                } catch (Exception e) {
                    log.error("[SAVE-USER-INFO] fail to change nickname and address!");
                    e.printStackTrace();
                    return ResultCode.FAIL.toResponseEntity();
                }
            }
        }
        return null;
    }



    @Override
    public ResponseEntity<?> getWorkShopDetails(Long id) {
        Optional<User> optionalUser = userRepository.findWorkshopById(id);

        checkUserData(optionalUser);

        log.info("[WORKSHOP-DETAILS] found workshop successfully!");
        return ResponseEntity.ok(new ResponseWorkShopDetailsDto(optionalUser.get()));
    }


    @Override
    public Optional<User> getUserByNickname(String nickname) {
        return userRepository.findUserByNickname(nickname);
    }

    @Override
    public ResponseEntity<?> savePhoneNumber(String phoneNumber, String nickname) {
        Optional<User> userOptional = userRepository.findUserByNickname(nickname);
        checkUserData(userOptional);

        try{
            User user = userOptional.get();
            user.updatePhone(phoneNumber);
            userRepository.save(user);
            return ResultCode.OK.toResponseEntity();
        }catch (Exception e){
            log.error("[SAVE-PHONE-NUMBER] fail to update phone number!");
            e.printStackTrace();
            return ResultCode.FAIL.toResponseEntity();
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

    private ResponseEntity<?> checkUserData(Optional<User> optionalUser){
        if(optionalUser.isEmpty()){
            log.error("[CHECK-USER-DATA] not found user!");
            return ResultCode.NOT_FOUND_USER.toResponseEntity();
        }
        User user = optionalUser.get();
        if(!user.isEnabled()){
            log.error("[CHECK-USER-DATA] deleted user!");
            return ResultCode.DELETED_USER.toResponseEntity();
        }
        return null;
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
    private double[] changeToCoordinate(String address){
        RestTemplate rt = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        headers.add("Authorization", KAKAO_API_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = rt.exchange(KAKAO_LOCAL_URL + address, HttpMethod.GET, entity,String.class);
        String body = response.getBody();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> responseMap = objectMapper.readValue(body, Map.class);
            List<Map<String, Object>> documents = (List<Map<String, Object>>) responseMap.get("documents");

            if (!documents.isEmpty()) {
                Map<String, Object> firstDocument = documents.get(0);
                double x = Double.parseDouble(firstDocument.get("x").toString());
                double y = Double.parseDouble(firstDocument.get("y").toString());
                return new double[]{x, y};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}

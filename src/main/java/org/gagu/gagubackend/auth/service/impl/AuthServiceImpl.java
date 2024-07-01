package org.gagu.gagubackend.auth.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.auth.dao.AuthDAO;
import org.gagu.gagubackend.auth.dto.request.RequestAuthDto;
import org.gagu.gagubackend.auth.service.AuthService;
import org.gagu.gagubackend.global.domain.CommonResponse;
import org.gagu.gagubackend.global.domain.enums.LoginType;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthDAO authDAO;
    @Value("${google.accesstoken.url}")
    private String googleAccessTokenUrl;
    @Value("${google.client.id}")
    private String googleClientId;
    @Value("${google.client.secret}")
    private String googleClientSecret;
    @Value("${google.redirect.uri}")
    private String googleRedirectUri;
    @Value("${google.userinfo.uri}")
    private String googleUserInfoUri;
    @Value("${google.people.uri}")
    private String googlePeopleUri;
    @Value("${kakao.client.id}")
    private String kakaoClientKey;
    @Value("${kakao.redirect.url}")
    private String kakaoRedirectUrl;
    @Value("${kakao.accesstoken.url}")
    private String kakaoAccessTokenUrl;
    @Value("${kakao.userinfo.url}")
    private String kakaoUserInfoUrl;

    @Override
    public ResponseEntity<?> signIn(String authorizeCode, String type) {
        switch (type){
            case "kakao":
                log.info("[kakao login] issue a authorizecode");
                ObjectMapper objectMapper = new ObjectMapper();
                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("grant_type", "authorization_code");
                params.add("client_id", kakaoClientKey);
                params.add("redirect_uri", kakaoRedirectUrl);
                params.add("code", authorizeCode);

                HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

                try{
                    ResponseEntity<String> response = restTemplate.exchange(
                            kakaoAccessTokenUrl,
                            HttpMethod.POST,
                            kakaoTokenRequest,
                            String.class
                    );
                    log.info("[kakao login] authorizecode issued successfully");
                    Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
                    String accessToken = (String) responseMap.get("access_token");

                    RequestAuthDto requestSignUpDto = getKakaoUserInfo(accessToken);

                    log.info("[kakao login] dto : {}", requestSignUpDto.toString());

                    return authDAO.login(requestSignUpDto);

                }catch (Exception e){
                    log.warn("[kakao login] fail authorizecode issued");
                    return ResponseEntity.status(ResultCode.PASSWORD_NOT_MATCH.getCode())
                            .body(CommonResponse.fail(ResultCode.PASSWORD_NOT_MATCH));
                }

            case "google":
                log.info("[google login] issue a authorizecode");
                objectMapper = new ObjectMapper();
                restTemplate = new RestTemplate();
                headers = new HttpHeaders();

                headers.add("Content-type", "application/x-www-form-urlencoded");

                params = new LinkedMultiValueMap<>();
                params.add("code", authorizeCode);
                params.add("client_id", googleClientId);
                params.add("client_secret", googleClientSecret);
                params.add("redirect_uri", googleRedirectUri);
                params.add("grant_type", "authorization_code");

                HttpEntity<MultiValueMap<String, String>> googleRequest = new HttpEntity<>(params, headers);

                ResponseEntity<String> response = restTemplate.postForEntity(googleAccessTokenUrl,googleRequest,String.class);
                log.info("[google login] authorizecode issued successfully");
                try{
                    Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
                    String accessToken = (String) responseMap.get("access_token");
                    log.info("[google login] access token issued successfully");
                    log.info("[google login] accessToken : {}",accessToken);

                    log.warn("[google login] get user info");
                    RequestAuthDto requestSignUpDto = getGoogleUserInfo(accessToken);

                    return authDAO.login(requestSignUpDto);
                }catch (Exception e){
                    e.printStackTrace();
                    log.warn("[google login] fail to issue authorizecode");
                    return ResponseEntity.status(ResultCode.PASSWORD_NOT_MATCH.getCode())
                            .body(CommonResponse.fail(ResultCode.PASSWORD_NOT_MATCH));
                }
        }
        return null;
    }

    private RequestAuthDto getKakaoUserInfo(String accessToken){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        ObjectMapper mapper = new ObjectMapper();

        headers.add("Authorization", "Bearer "+accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("secure_resource", "true");

        HttpEntity<?> entity = new HttpEntity<>(requestBody,headers);

        ResponseEntity<String> response = restTemplate.postForEntity(kakaoUserInfoUrl,entity,String.class);

        try{
            Map<String, Object> responseMap = mapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
            Map<String, Object> kakaoAccount = (Map<String, Object>) responseMap.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            RequestAuthDto requestSignUpDto = RequestAuthDto.builder()
                    .name((String) kakaoAccount.get("name"))
                    .nickName(null)
                    .password(getRandomPassword())
                    .phoneNumber((String) kakaoAccount.get("phone_number"))
                    .email((String)kakaoAccount.get("email"))
                    .profileUrl((String) profile.get("profile_image_url"))
                    .loginType(LoginType.KAKAO.toString())
                    .useAble(true)
                    .build();

            return requestSignUpDto;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private RequestAuthDto getGoogleUserInfo(String accessToken){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        ObjectMapper mapper = new ObjectMapper();
        headers.add("Authorization","Bearer " + accessToken);

        HttpEntity<MultiValueMap<String, String>> userInfoRequest = new HttpEntity<>(headers);
        ResponseEntity<String> userInfoResponse = restTemplate.exchange(googleUserInfoUri,HttpMethod.GET,userInfoRequest,String.class);
        log.info("[google user info] : {}",userInfoResponse.toString());
        try{
            Map<String, Object> responseMap = mapper.readValue(userInfoResponse.getBody(), new TypeReference<Map<String, Object>>() {});
            String nickName = getRandomNickName((String)responseMap.get("name"),(String)responseMap.get("id"));

            RequestAuthDto requestSignUpDto = RequestAuthDto.builder()
                    .name((String) responseMap.get("name"))
                    .nickName(null)
                    .password(getRandomPassword())
                    .phoneNumber(null)
                    .email((String)responseMap.get("email"))
                    .profileUrl((String) responseMap.get("picture"))
                    .loginType(LoginType.GOOGLE.toString())
                    .useAble(true)
                    .build();

            return requestSignUpDto;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    private String getRandomPassword() {
        String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        String NUMBER = "0123456789";

        String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
        SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);

            sb.append(rndChar);
        }

        return sb.toString();
    }
    private String getRandomNickName(String name, String number) {
        String CHAR_LOWER = name;
        String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        String NUMBER = number;

        String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
        SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);

            sb.append(rndChar);
        }

        return sb.toString();
    }
}

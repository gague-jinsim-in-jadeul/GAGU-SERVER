package org.gagu.gagubackend.auth.controller;

import com.amazonaws.Response;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.gagu.gagubackend.auth.dto.request.*;
import org.gagu.gagubackend.auth.service.AuthService;
import org.gagu.gagubackend.global.config.RedisConfig;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.gagu.gagubackend.global.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final AmazonS3Client amazonS3Client;
    private final RedisConfig redisConfig;
    private DefaultMessageService messageService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${coolsms.fromnumber}")
    private String MESSAGE_SENDER;
    @Value("${coolsms.apikey}")
    private String COOLSMS_APIKEY;
    @Value("${coolsms.apisecret}")
    private String COOLSMS_SECRET_KEY;
    @Value("${coolsms.domain}")
    private String COOLSMS_DOMAIN;
    @PostConstruct
    public void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(COOLSMS_APIKEY, COOLSMS_SECRET_KEY, COOLSMS_DOMAIN);
    }

    @Operation(summary = "구글 소셜 로그인 콜백 컨트롤러 입니다.")
    @GetMapping("/google/callback")
    public ResponseEntity<?> getGoogleAuthorizeCode(@RequestParam("code") String authorizeCode, String type){
        type = "google";
        log.info("[google login] authorizeCode : {}", authorizeCode);
        return authService.signIn(authorizeCode, type);
    }
    @Operation(summary = "카카오 소셜 로그인 콜백 컨트롤러 입니다.")
    @GetMapping("/kakao/callback")
    public ResponseEntity<?> getKaKaoAuthorizeCode(@RequestParam("code") String authorizeCode, String type){
        type = "kakao";
        log.info("[kakao login] authorizeCode : {}", authorizeCode);
        return authService.signIn(authorizeCode, type);
    }
    @Operation(summary = "구글 소셜 로그인 컨트롤러 입니다.")
    @PostMapping("/google/sign")
    public ResponseEntity<?> googleSign(@RequestBody RequestOauthSignDto requestOauthSignDto){
        String type = "google";
        return authService.normalSignIn(requestOauthSignDto, type);
    }
    @Operation(summary = "카카오 소셜 로그인 컨트롤러 입니다.")
    @PostMapping("/kakao/sign")
    public ResponseEntity<?> kaKaoSign(@RequestBody RequestOauthSignDto requestOauthSignDto){
        String type = "kakao";
        return authService.normalSignIn(requestOauthSignDto, type);
    }
    @Operation(summary = "일반 회원가입 컨트롤러 입니다.")
    @PostMapping("/general/sign-up")
    public ResponseEntity<?> generalSignUp(@RequestBody RequestGeneralSignUpDto requestGeneralSignUpDto){
        String type = "general";
        return authService.generalSingUp(requestGeneralSignUpDto, type);
    }
    @Operation(summary = "일반 로그인 컨트롤러 입니다.")
    @PostMapping("/general/sign-in")
    public ResponseEntity<?> generalSignIn(@RequestBody RequestGeneralSignDto requestGeneralSignDto){
        String type = "general";
        return authService.generalSignIn(requestGeneralSignDto, type);
    }
    @Operation(summary = "사용자 프로필 사진 업로드", description = "사용자가 회원가입 시 프로필을 업로드합니다.")
    @PostMapping("/profile-upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file){

        log.info("[file-upload] file : {}",file.getOriginalFilename());
        try{
            String filename = file.getOriginalFilename();
            ObjectMetadata data = new ObjectMetadata();

            data.setContentType(file.getContentType()); // 파일 타입
            data.setContentLength(file.getSize()); // 파일 사이즈

            amazonS3Client.putObject(bucket, filename, file.getInputStream(), data);

            return ResponseEntity.ok(amazonS3Client.getUrl(bucket, file.getOriginalFilename()).toString());

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "인증번호 인증", description = "공방 회원가입 시 인증번호를 인증합니다.")
    @PostMapping("/authorize")
    public ResponseEntity<?> checkAuthorizationNumber(@RequestBody RequestAuthorizePhone requestAuthorizePhone){
        if(requestAuthorizePhone.getAuthorizationNumber() == null){
            log.error("[authorize phone number] no authorization number");
            return ResultCode.NO_AUTHORIZE_NUMBER.toResponseEntity();
        }
        if(requestAuthorizePhone.getPhoneNumber() == null){
            log.error("[authorize phone number] no phone number!");
            return ResultCode.BAD_REQUEST.toResponseEntity();
        }

        String number = (String) redisConfig.redisTemplate().opsForValue().get(requestAuthorizePhone.getPhoneNumber());

        if(number.equals(requestAuthorizePhone.getAuthorizationNumber())){
            log.info("[authorize phone number]");
            return ResponseEntity.ok().body("인증에 성공하셨습니다.");
        }else{
            return ResponseEntity.status(400).body("인증에 실패하셨습니다.");
        }
    }

    @Operation(summary = "인증번호 발급", description = "공방 회원가입 시 인증번호를 발송합니다.")
    @PostMapping("/send-one")
    public ResponseEntity<?> sendOne(@RequestBody RequestPhoneNumber requestPhoneNumber) {

        if(requestPhoneNumber.getPhoneNumber().isEmpty()){
            return ResultCode.BAD_REQUEST.toResponseEntity();
        }


        Message message = new Message();

        message.setFrom(MESSAGE_SENDER);
        message.setTo(requestPhoneNumber.getPhoneNumber());

        String number = getRandomNumber(); // 랜덤 인증 번호 생성
        message.setText(number);

        SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));

        redisConfig.redisTemplate().opsForValue().set(requestPhoneNumber.getPhoneNumber(), // redis put {phone : 인증번호}
                number,
                180000,
                TimeUnit.MILLISECONDS
        );

        return ResponseEntity.ok().body(response.getStatusMessage());
    }

    @Operation(summary = "사용자 로그인 후 로그아웃 기능입니다.", description = "jwt 만료")
    @DeleteMapping("/log-out")
    public ResponseEntity<?> logOut(HttpServletRequest request){
        String token = jwtTokenProvider.extractToken(request);

        if(token == null){
            log.error("[logout] token is null!");
            return ResultCode.TOKEN_IS_NULL.toResponseEntity();
        }
        log.info("[logout] token : {}", token);
        return authService.logOut(token);
    }

    private String getRandomNumber() {
        log.info("[sendNumber] creating random number..");
        String NUMBER = "0123456789";

        String DATA_FOR_RANDOM_STRING = NUMBER;
        SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);

            sb.append(rndChar);
        }

        log.info("[sendNumber] result : {}", sb.toString());

        return sb.toString();
    }
}

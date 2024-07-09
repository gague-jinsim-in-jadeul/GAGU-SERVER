package org.gagu.gagubackend.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.auth.dto.request.RequestGeneralSignDto;
import org.gagu.gagubackend.auth.dto.request.RequestGeneralSignUpDto;
import org.gagu.gagubackend.auth.dto.request.RequestSaveUserDto;
import org.gagu.gagubackend.auth.dto.request.RequestOauthSignDto;
import org.gagu.gagubackend.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
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
}

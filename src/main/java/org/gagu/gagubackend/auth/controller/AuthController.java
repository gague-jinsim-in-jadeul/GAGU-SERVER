package org.gagu.gagubackend.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.auth.dto.request.RequestSignDto;
import org.gagu.gagubackend.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    @GetMapping("/google/callback")
    public ResponseEntity<?> getGoogleAuthorizeCode(@RequestParam("code") String authorizeCode, String type){
        type = "google";
        log.info("[google login] authorizeCode : {}", authorizeCode);
        return authService.signIn(authorizeCode, type);
    }
    @GetMapping("/kakao/callback")
    public ResponseEntity<?> getKaKaoAuthorizeCode(@RequestParam("code") String authorizeCode, String type){
        type = "kakao";
        log.info("[kakao login] authorizeCode : {}", authorizeCode);
        return authService.signIn(authorizeCode, type);
    }
    @PostMapping("/google/sign")
    public ResponseEntity<?> googleSign(@RequestBody RequestSignDto requestSignDto){
        String type = "google";
        return authService.normalSignIn(requestSignDto, type);
    }
    @PostMapping("/kakao/sign")
    public ResponseEntity<?> kaKaoSign(@RequestBody RequestSignDto requestSignDto){
        String type = "kakao";
        return authService.normalSignIn(requestSignDto, type);
    }
}

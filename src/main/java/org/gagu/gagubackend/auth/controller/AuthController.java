package org.gagu.gagubackend.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}

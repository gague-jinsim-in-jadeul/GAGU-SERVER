package org.gagu.gagubackend.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.auth.service.AuthService;
import org.gagu.gagubackend.global.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    @Operation(summary = "사용자 프로필 변경", description = "사용자가 회원가입 후 프로필을 변경합니다.")
    @PostMapping("/reset")
    public ResponseEntity<?> changeFile(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        log.info("[file-upload] file : {}", file.getOriginalFilename());

        String token = jwtTokenProvider.extractToken(request);
        String nickName = jwtTokenProvider.getUserNickName(token);

        return authService.changeProfile(file, nickName);
    }
}

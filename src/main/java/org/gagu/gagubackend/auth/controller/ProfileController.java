package org.gagu.gagubackend.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.auth.dto.request.RequestAddressDto;
import org.gagu.gagubackend.auth.dto.request.RequestChangeUserInfoDto;
import org.gagu.gagubackend.auth.service.AuthService;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.gagu.gagubackend.global.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    @Operation(summary = "사용자 프로필 조회", description = "회원가입 시 설정된 프로필을 반환합니다.")
    @GetMapping("/info")
    public ResponseEntity<?> getProfile(HttpServletRequest request){
        String token = jwtTokenProvider.extractToken(request);

        if(token.isEmpty()){
            return ResponseEntity.status(ResultCode.NOT_FOUND_TOKEN.getCode()).body(ResultCode.NOT_FOUND_TOKEN.getMessage());
        }
        if(!jwtTokenProvider.validateToken(token)){
            return ResponseEntity.status(ResultCode.EXPIRED_TOKEN.getCode()).body(ResultCode.EXPIRED_TOKEN.getMessage());
        }
        String nickname = jwtTokenProvider.getUserNickName(token);
        return authService.getProfile(nickname);
    }

    @Operation(summary = "사용자 주소 저장", description = "사용자 주소 입력 시 저장합니다.")
    @PostMapping("/address")
    public ResponseEntity<?> updateAddress(@RequestBody RequestAddressDto requestAddressDto, HttpServletRequest request){
        String token = jwtTokenProvider.extractToken(request);
        if(token.isEmpty()){
            return ResultCode.NOT_FOUND_TOKEN.toResponseEntity();
        }
        if(requestAddressDto.getAddress().isEmpty()){
            return ResultCode.BAD_REQUEST.toResponseEntity();
        }
        String nickName = jwtTokenProvider.getUserNickName(token);

        return authService.saveAddress(requestAddressDto, nickName);
    }
    @Operation(summary = "사용자 정보 변경", description = "로그인 후 자신의 사용자 정보를 변경합니다.")
    @PostMapping("/user-info/reset")
    public ResponseEntity<?> updateUserInfo(@RequestBody RequestChangeUserInfoDto requestChangeUserInfoDto,
                                            HttpServletRequest request){
        String token = jwtTokenProvider.extractToken(request);
        if(token.isEmpty()){
            return ResultCode.NOT_FOUND_TOKEN.toResponseEntity();
        }
        String nickName = jwtTokenProvider.getUserNickName(token);

        if(requestChangeUserInfoDto.getEmail() == null){
            return ResultCode.BAD_REQUEST.toResponseEntity();
        }
        if(requestChangeUserInfoDto.getAddress() == null){
            return ResultCode.BAD_REQUEST.toResponseEntity();
        }

        return authService.updateUserInfo(requestChangeUserInfoDto, nickName);
    }
}

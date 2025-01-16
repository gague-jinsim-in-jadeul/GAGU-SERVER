package org.gagu.gagubackend.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.auth.domain.User;
import org.gagu.gagubackend.auth.dto.request.RequestAddressDto;
import org.gagu.gagubackend.auth.dto.request.RequestChangeUserInfoDto;
import org.gagu.gagubackend.auth.repository.UserRepository;
import org.gagu.gagubackend.auth.service.AuthService;
import org.gagu.gagubackend.auth.service.ReviewService;
import org.gagu.gagubackend.global.domain.enums.FilterType;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.gagu.gagubackend.global.security.JwtTokenProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    private final ReviewService reviewService;
    private final UserRepository userRepository;
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

        if(requestChangeUserInfoDto.getAddress() == null){
            return ResultCode.BAD_REQUEST.toResponseEntity();
        }
        return authService.updateUserInfo(requestChangeUserInfoDto, nickName);
    }
    @Operation(summary = "공방 조회", description = "가구 제작 의뢰를 맡길 공방을 반환합니다.")
    @GetMapping("/workshops")
    public ResponseEntity<?> getWorkshops(@RequestParam FilterType filtertype, @RequestParam int page, HttpServletRequest request){
        Pageable pageable = PageRequest.of(page, 3);

        String token = jwtTokenProvider.extractToken(request);
        if(token.isEmpty()){
            return ResultCode.NOT_FOUND_TOKEN.toResponseEntity();
        }

        String nickName = jwtTokenProvider.getUserNickName(token);

        Double longitude = (Double) request.getSession().getAttribute("longitude");
        Double latitude = (Double) request.getSession().getAttribute("latitude");
        if(longitude==null || latitude ==null){
            Optional<User> optionalUser = userRepository.findUserByNickname(nickName);
            if(optionalUser.isPresent()){
                User user = optionalUser.get();
                longitude = user.getLongitude();
                latitude = user.getLatitude();
                request.getSession().setAttribute("longitude",longitude);
                request.getSession().setAttribute("latitude",latitude);
            }
        }
        return ResponseEntity.ok(reviewService.getAllWorkShop(filtertype,pageable,longitude,latitude));
    }
    @Operation(summary = "공방 디테일 조회", description = "가구 제작 의뢰를 맡길 공방의 자세한 정보를 반환합니다.")
    @GetMapping("/workshop/{id}")
    public ResponseEntity<?> getWorkShopDetail(@PathVariable Long id){
        if(id == null){
            return ResultCode.BAD_REQUEST.toResponseEntity();
        }
        return authService.getWorkShopDetails(id);
    }
    @Operation(summary = "전화번호 저장", description = "로그인 후 전화번호를 저장합니다.")
    @PostMapping("/save-phone")
    public ResponseEntity<?> savePhoneNumber(@RequestBody String phoneNumber, HttpServletRequest request){
        String nickName = jwtTokenProvider.getUserNickName(jwtTokenProvider.extractToken(request));

        if(phoneNumber == null || nickName == null){
            return ResultCode.BAD_REQUEST.toResponseEntity();
        }
        return authService.savePhone(phoneNumber, nickName);
    }
}

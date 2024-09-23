package org.gagu.gagubackend.estimate.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.estimate.dto.request.RequestSaveFurnitureDto;
import org.gagu.gagubackend.estimate.service.EstimateService;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.gagu.gagubackend.global.security.JwtTokenProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/estimate")
@Slf4j
@RequiredArgsConstructor
public class EstimateController {
    private final JwtTokenProvider jwtTokenProvider;
    private final EstimateService estimateService;

    @Operation(summary = "가구 이미지 저장", description = "생성한 2D, 3D 이미지를 저장 합니다.")
    @PostMapping("/save-furniture")
    public ResponseEntity<?> saveFurniture(@RequestBody RequestSaveFurnitureDto requestSaveFurnitureDto,
                                           HttpServletRequest request){
        String token = jwtTokenProvider.extractToken(request);
        String nickname = jwtTokenProvider.getUserNickName(token);
        log.info("[SAVE-FURNITURE] save {}'s 2D, 3D furniture", nickname);

        if(isAnyFieldNull(requestSaveFurnitureDto)){
            return ResultCode.BAD_REQUEST.toResponseEntity();
        }
        return estimateService.saveFurniture(requestSaveFurnitureDto, nickname);
    }

    @Operation(summary = "저장한 가구 반환", description = "사용자가 생성한 2D, 3D 이미지들을 반환합니다.")
    @GetMapping("/get-furniture")
    public ResponseEntity<?> getFurnitures(HttpServletRequest request,
                                           @RequestParam(defaultValue = "0") int page){
        String token = jwtTokenProvider.extractToken(request);
        if(token.isEmpty()){
            return ResultCode.TOKEN_IS_NULL.toResponseEntity();
        }
        String nickname = jwtTokenProvider.getUserNickName(token);
        log.info("[GET-FURNITURE] get {}'s 2D, 3D furniture", nickname);

        Pageable pageable = PageRequest.of(page,4, Sort.Direction.DESC,"createdTime");
        return ResponseEntity.ok(estimateService.getFurniture(nickname,pageable));
    }

    @Operation(summary = "저장한 가구 삭제", description = "사용자가 저장한 2D, 3D 이미지를 삭제합니다.")
    @DeleteMapping("/remove-furniture")
    public ResponseEntity<?> removeFurnitures(@RequestParam Long id){
        if(id == null){
            log.error("[DELETE-FURNITURE] id must be required!");
            return ResultCode.BAD_REQUEST.toResponseEntity();
        }
        return estimateService.deleteFurniture(id);
    }

    /**
     * RequestSaveFurnitureDto null 확인 메서드
     * @param dto
     * @return null 값 -> true
     */
    private boolean isAnyFieldNull(RequestSaveFurnitureDto dto){
        return Stream.of(dto.getFurnitureName(),
                dto.getFurniture2DUrl(),
                dto.getFurniture3DObj(),
                dto.getFurniture3DMtl(),
                dto.getFurniture3DTexture1(),
                dto.getFurniture3DTexture2())
                .anyMatch(Objects::isNull);
    }
}

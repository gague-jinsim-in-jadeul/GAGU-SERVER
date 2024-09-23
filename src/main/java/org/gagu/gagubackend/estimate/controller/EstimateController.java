package org.gagu.gagubackend.estimate.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.estimate.dto.request.RequestSaveFurnitureDto;
import org.gagu.gagubackend.estimate.service.EstimateService;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.gagu.gagubackend.global.security.JwtTokenProvider;
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

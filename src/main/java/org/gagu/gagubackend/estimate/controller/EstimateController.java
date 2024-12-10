package org.gagu.gagubackend.estimate.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.chat.dto.request.EstimateChatContentsDto;
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

        Pageable pageable = PageRequest.of(page,4, Sort.Direction.DESC,"createdDate");
        return ResponseEntity.ok(estimateService.getFurniture(nickname,pageable));
    }

    @Operation(summary = "견적서 작성", description = "공방관계자가 사용자가 제작을 희망하는 가구의 가격과 의견을 작성합니다.")
    @PostMapping("/save")
    public ResponseEntity<?> saveEstimate(HttpServletRequest request, @RequestBody EstimateChatContentsDto dto){
        if(dto.getId() == null){
            return ResultCode.FAIL.toResponseEntity();
        } else if (dto.getPrice() == null) {
            return ResultCode.FAIL.toResponseEntity();
        } else if (dto.getDescription() == null) {
            return ResultCode.FAIL.toResponseEntity();
        }else{
            String token = jwtTokenProvider.extractToken(request);
            String nickName = jwtTokenProvider.getUserNickName(token);

            return estimateService.saveEstimate(dto, nickName);
        }
    }

    @Operation(summary = "견적서 반환", description = "견적서가 발행된 저장된 가구들이 반환됩니다.")
    @GetMapping("/estimates")
    public ResponseEntity<?> getEstimates(HttpServletRequest request, @RequestParam(defaultValue = "0") int page){
        String token = jwtTokenProvider.extractToken(request);
        String nickname = jwtTokenProvider.getUserNickName(token);

        Pageable pageable = PageRequest.of(page,4, Sort.Direction.DESC,"modifiedDate");
        return ResponseEntity.ok(estimateService.getEstimate(nickname,pageable));
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
    @Operation(summary = "의뢰된 가구 이미지 조회", description = "사용자가 가구 제작 후 의뢰한 가구 이미지를 공방관계자가 조회합니다.")
    @GetMapping("/workshop/{requester}")
    public ResponseEntity<?> getRequestFurnitures(HttpServletRequest request, @RequestParam(defaultValue = "0") int page, @PathVariable String requester){
        String token = jwtTokenProvider.extractToken(request);
        String nickname = jwtTokenProvider.getUserNickName(token);

        Pageable pageable = PageRequest.of(page,4, Sort.Direction.DESC,"modifiedDate");

        return ResponseEntity.ok(estimateService.getRequestFurnitures(pageable, nickname, requester));
    }

    /**
     * RequestSaveFurnitureDto null 확인 메서드
     * @param dto
     * @return null 값 -> true
     */
    private boolean isAnyFieldNull(RequestSaveFurnitureDto dto){
        return Stream.of(dto.getFurnitureName(),
                dto.getFurniture2DUrl(),
                dto.getFurnitureGlbUrl(),
                dto.getFurnitureGltfUrl())
                .anyMatch(Objects::isNull);
    }
}

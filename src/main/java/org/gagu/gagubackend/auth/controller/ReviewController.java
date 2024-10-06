package org.gagu.gagubackend.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.auth.dto.request.RequestWriteReviewDto;
import org.gagu.gagubackend.auth.service.ReviewService;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.gagu.gagubackend.global.security.JwtTokenProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {
    private final JwtTokenProvider jwtTokenProvider;
    private final ReviewService reviewService;
    @Operation(summary = "공방 리뷰 쓰기", description = "사용자가 공방에 대한 리뷰를 작성합니다.")
    @PostMapping("/write")
    public ResponseEntity<?> writeReview(@RequestBody RequestWriteReviewDto requestWriteReviewDto,
                                         HttpServletRequest request){
            String token = jwtTokenProvider.extractToken(request);
            if(token == null){
                return ResultCode.TOKEN_IS_NULL.toResponseEntity();
            }

            String nickName = jwtTokenProvider.getUserNickName(token);

            // 요청 값 중 null 이면
            if((requestWriteReviewDto.getWorkshopName() == null) ||
                    (requestWriteReviewDto.getDescription() == null) ||
                    (requestWriteReviewDto.getStars() == null)){
                return ResultCode.BAD_REQUEST.toResponseEntity();
            }

            return reviewService.postReview(requestWriteReviewDto, nickName);
    }
    @Operation(summary = "공방 리뷰 조회", description = "사용자가 해당하는 공방의 리뷰를 조회합니다.")
    @GetMapping("/{workshopName}")
    public ResponseEntity<?> getReviews(@PathVariable String workshopName, @RequestParam(defaultValue = "0") int page){
        if(workshopName == null){
            return ResultCode.FAIL.toResponseEntity();
        }

        Pageable pageable = PageRequest.of(page, 3, Sort.Direction.DESC,"modifiedDate");
        return ResponseEntity.ok(reviewService.getReviews(pageable, workshopName));
    }
}

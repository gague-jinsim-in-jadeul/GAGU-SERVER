package org.gagu.gagubackend.auth.service;

import org.gagu.gagubackend.auth.dto.request.RequestWriteReviewDto;
import org.gagu.gagubackend.auth.dto.response.ResponseWorkshopDto;
import org.gagu.gagubackend.chat.dto.response.ResponseReviewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface ReviewService {
    /**
     * 리뷰 작성
     * @param dto
     * @param nickname
     * @return 상태값
     */
    ResponseEntity<?> postReview(RequestWriteReviewDto dto, String nickname);

    /**
     * 공방 리뷰 조회
     * @param pageable
     * @param workshopName
     * @return ResponseReviewDto
     */
    Page<ResponseReviewDto> getReviews(Pageable pageable, String workshopName);

    /**
     * 모든 공방 조회
     * @param pageable
     * @return ResponseWorkshopDto
     */
    Page<ResponseWorkshopDto> getAllWorkShop(Pageable pageable);
}

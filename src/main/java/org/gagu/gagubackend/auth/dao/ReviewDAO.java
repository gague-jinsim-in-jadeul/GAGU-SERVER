package org.gagu.gagubackend.auth.dao;

import org.gagu.gagubackend.auth.dto.request.RequestWriteReviewDto;
import org.gagu.gagubackend.auth.dto.response.ResponseWorkshopDto;
import org.gagu.gagubackend.chat.dto.response.ResponseReviewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface ReviewDAO {
    /**
     * 리뷰 저장
     * @param dto
     * @param nickname
     * @return
     */
    ResponseEntity<?> saveReview(RequestWriteReviewDto dto, String nickname);

    /**
     * 공방 리뷰들 조회
     * @param pageable
     * @param workshopName
     * @return
     */
    Page<ResponseReviewDto> getReviews(Pageable pageable, String workshopName);

    /**
     * 모든 공방 조회
     * @param pageable
     * @return
     */
    Page<ResponseWorkshopDto> getAllWorkShop(Pageable pageable);
}

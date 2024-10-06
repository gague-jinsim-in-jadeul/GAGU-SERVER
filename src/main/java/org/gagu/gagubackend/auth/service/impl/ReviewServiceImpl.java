package org.gagu.gagubackend.auth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.auth.dao.ReviewDAO;
import org.gagu.gagubackend.auth.dto.request.RequestWriteReviewDto;
import org.gagu.gagubackend.auth.dto.response.ResponseWorkshopDto;
import org.gagu.gagubackend.auth.service.ReviewService;
import org.gagu.gagubackend.chat.dto.response.ResponseReviewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewDAO reviewDAO;
    @Override
    public ResponseEntity<?> postReview(RequestWriteReviewDto dto, String nickname) {
        return reviewDAO.saveReview(dto, nickname);
    }

    @Override
    public Page<ResponseReviewDto> getReviews(Pageable pageable, String workshopName) {
        return reviewDAO.getReviews(pageable, workshopName);
    }

    @Override
    public Page<ResponseWorkshopDto> getAllWorkShop(Pageable pageable) {
        return reviewDAO.getAllWorkShop(pageable);
    }
}

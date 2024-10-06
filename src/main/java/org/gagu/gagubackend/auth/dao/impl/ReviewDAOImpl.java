package org.gagu.gagubackend.auth.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.auth.dao.ReviewDAO;
import org.gagu.gagubackend.auth.domain.Review;
import org.gagu.gagubackend.auth.domain.StarReview;
import org.gagu.gagubackend.auth.dto.request.RequestWriteReviewDto;
import org.gagu.gagubackend.auth.dto.response.ResponseWorkshopDto;
import org.gagu.gagubackend.auth.repository.ReviewRepository;
import org.gagu.gagubackend.auth.repository.StarReviewRepository;
import org.gagu.gagubackend.chat.dto.response.ResponseReviewDto;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReviewDAOImpl implements ReviewDAO {
    private final ReviewRepository reviewRepository;
    private final StarReviewRepository starReviewRepository;
    @Override
    public ResponseEntity<?> saveReview(RequestWriteReviewDto dto, String nickname) {
        log.info("[SAVE-REVIEW] saving review.. ");
        try{
            Review review = Review.builder()
                    .writer(nickname)
                    .workshopName(dto.getWorkshopName())
                    .description(dto.getDescription())
                    .reviewPhoto1(dto.getReviewPhoto1())
                    .reviewPhoto2(dto.getReviewPhoto2())
                    .reviewPhoto3(dto.getReviewPhoto3())
                    .stars(dto.getStars())
                    .build();

            reviewRepository.save(review);

            log.info("[SAVE-REVIEW] updating star average..");
            StarReview starReview = starReviewRepository.findByWorkshopName(dto.getWorkshopName());
            BigDecimal sum = starReview.getSum().add(dto.getStars());
            BigInteger count = starReview.getCount().add(new BigInteger("1"));
            BigDecimal average = sum.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);

            starReview.setCount(count);
            starReview.setStarsAverage(average);
            starReview.setSum(sum);
            starReviewRepository.save(starReview);
            log.info("[SAVE-REVIEW] update star average success!");

            log.info("[SAVE-REVIEW] save review success!");
            return ResultCode.OK.toResponseEntity();
        }catch (Exception e){
            log.error("[SAVE-REVIEW] fail to save review");
            e.printStackTrace();
            return ResultCode.FAIL.toResponseEntity();
        }
    }

    @Override
    public Page<ResponseReviewDto> getReviews(Pageable pageable, String workshopName) {
        log.info("[GET-REVIEWS] checking reviews..");
        try{
            Page<Review> reviews = reviewRepository.findByWorkshopName(workshopName, pageable);

            List<ResponseReviewDto> dtoList = reviews.stream()
                    .map(review -> {
                        ResponseReviewDto dto = new ResponseReviewDto();
                        dto.setWriter(review.getWriter());
                        dto.setDate(review.getModifiedDate());
                        dto.setDescription(review.getDescription());
                        dto.setStars(review.getStars());
                        dto.setImg1(review.getReviewPhoto1());
                        dto.setImg2(review.getReviewPhoto2());
                        dto.setImg3(review.getReviewPhoto3());

                        return dto;
                    }).collect(Collectors.toList());
            log.info("[GET-REVIEWS] collect reviews success!");
            return new PageImpl<>(dtoList, pageable, reviews.getTotalElements());
        }catch (Exception e){
            log.error("[GET-REVIEWS] fail to check reviews!");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Page<ResponseWorkshopDto> getAllWorkShop(Pageable pageable) {
        Page<StarReview> starReviews = starReviewRepository.findAll(pageable);
        log.info("[GET-ALL-WORKSHOP] checking workshops..");
        List<ResponseWorkshopDto> dtos = starReviews.stream()
                .map(starReview -> {
                    ResponseWorkshopDto dto = new ResponseWorkshopDto();
                    dto.setId(starReview.getWorkshop().getId());
                    dto.setCount(starReview.getCount());
                    dto.setStarAverage(starReview.getStarsAverage());
                    dto.setThumbnail(starReview.getWorkshop().getProfileUrl());
                    dto.setAddress(starReview.getWorkshop().getAddress());
                    dto.setDescription(starReview.getWorkshop().getProfileMessage());
                    dto.setWorkshopName(starReview.getWorkshopName());

                    return dto;
                }).collect(Collectors.toList());
        log.info("[GET-ALL-WORKSHOP] collect workshop success!");
        return new PageImpl<>(dtos, pageable, starReviews.getTotalElements());
    }
}

package org.gagu.gagubackend.estimate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.estimate.dao.EstimateDAO;
import org.gagu.gagubackend.estimate.domain.Estimate;
import org.gagu.gagubackend.estimate.dto.request.RequestSaveFurnitureDto;
import org.gagu.gagubackend.estimate.repository.EstimateRepository;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

@Component
@Slf4j
@RequiredArgsConstructor
public class EstimateDAOImpl implements EstimateDAO {
    private final EstimateRepository estimateRepository;
    @Override
    public ResponseEntity<?> saveFurniture(RequestSaveFurnitureDto requestSaveFurnitureDto, String nickname) {
        Date nowDate = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
        String formattedDate = simpleDateFormat.format(nowDate);

        Estimate estimate = Estimate.builder()
                .nickName(nickname)
                .furnitureName(requestSaveFurnitureDto.getFurnitureName())
                .furniture2DUrl(requestSaveFurnitureDto.getFurniture2DUrl())
                .furniture3DObj(requestSaveFurnitureDto.getFurniture3DObj())
                .furniture3DMtl(requestSaveFurnitureDto.getFurniture3DMtl())
                .furniture3DTexture1(requestSaveFurnitureDto.getFurniture3DTexture1())
                .furniture3DTexture2(requestSaveFurnitureDto.getFurniture3DTexture2())
                .createdTime(formattedDate)
                .build();
        try{
            estimateRepository.save(estimate);
            log.info("[SAVE-FURNITURE] save furniture img successfully!");
            return ResultCode.OK.toResponseEntity();
        }catch (Exception e){
            log.error("[SAVE-FURNITURE] fail to save furniture img!");
            e.printStackTrace();
            return ResultCode.FAIL.toResponseEntity();
        }
    }
}

package org.gagu.gagubackend.estimate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.estimate.dao.EstimateDAO;
import org.gagu.gagubackend.estimate.domain.Estimate;
import org.gagu.gagubackend.estimate.dto.request.RequestSaveFurnitureDto;
import org.gagu.gagubackend.estimate.dto.response.ResponseMyFurnitureDto;
import org.gagu.gagubackend.estimate.repository.EstimateRepository;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public Page<ResponseMyFurnitureDto> getMyFurniture(String nickname, Pageable pageable) {
        try{
            log.info("[GET-MY-FURNITURE] collecting my furnitures...");
            Page<Estimate> estimates = estimateRepository.findByNickName(nickname, pageable);

            List<ResponseMyFurnitureDto> responseMyFurnitureDtos = estimates.stream()
                    .map(estimate -> {
                        ResponseMyFurnitureDto dto = new ResponseMyFurnitureDto();
                        dto.setId(estimate.getId());
                        dto.setFurniture2DUrl(estimate.getFurniture2DUrl());
                        dto.setFurniture3DObj(estimate.getFurniture3DObj());
                        dto.setFurniture3DMtl(estimate.getFurniture3DMtl());
                        dto.setFurniture3DTexture1(estimate.getFurniture3DTexture1());
                        dto.setFurniture3DTexture2(estimate.getFurniture3DTexture2());
                        dto.setFurnitureName(estimate.getFurnitureName());
                        dto.setCreatedDate(estimate.getCreatedTime());
                        return dto;
                    }).collect(Collectors.toList());

            return new PageImpl<>(responseMyFurnitureDtos, pageable, estimates.getTotalElements());
        }catch (Exception e){
            log.error("[GET-MY-FURNITURE] fail to collect my furnitures");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ResponseEntity<?> deleteFurniture(Long id) {
        try{
            log.info("[DELETE-MY-FURNITURE] deleting my furniture...");
            estimateRepository.deleteById(id);
            log.info("[DELETE-MY-FURNITURE] delete my furniture successfully!");
            return ResultCode.OK.toResponseEntity();
        }catch (Exception e){
            log.error("[DELETE-MY-FURNITURE] fail to delete my furniture!");
            e.printStackTrace();
            return ResultCode.FAIL.toResponseEntity();
        }
    }
}
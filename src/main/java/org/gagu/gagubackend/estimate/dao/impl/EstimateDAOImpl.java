package org.gagu.gagubackend.estimate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.auth.domain.User;
import org.gagu.gagubackend.auth.repository.UserRepository;
import org.gagu.gagubackend.chat.dto.request.EstimateChatContentsDto;
import org.gagu.gagubackend.estimate.dao.EstimateDAO;
import org.gagu.gagubackend.estimate.domain.Estimate;
import org.gagu.gagubackend.estimate.dto.request.RequestSaveFurnitureDto;
import org.gagu.gagubackend.estimate.dto.response.ResponseCompleteEstimate;
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
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class EstimateDAOImpl implements EstimateDAO {
    private final EstimateRepository estimateRepository;
    private final UserRepository userRepository;
    @Override
    public ResponseEntity<?> saveFurniture(RequestSaveFurnitureDto requestSaveFurnitureDto, String nickname) {

        User user = userRepository.findByNickName(nickname);

        Estimate estimate = Estimate.builder()
                .nickName(user)
                .furnitureName(requestSaveFurnitureDto.getFurnitureName())
                .furniture2DUrl(requestSaveFurnitureDto.getFurniture2DUrl())
                .furnitureGlbUrl(requestSaveFurnitureDto.getFurnitureGlbUrl())
                .furnitureGltfUrl(requestSaveFurnitureDto.getFurnitureGltfUrl())
                .createdTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd a HH시 mm분 ss초")))
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
            User user = userRepository.findByNickName(nickname);
            Page<Estimate> estimates = estimateRepository.findByNickName(user, pageable);

            List<ResponseMyFurnitureDto> responseMyFurnitureDtos = estimates.stream()
                    .map(estimate -> {
                        ResponseMyFurnitureDto dto = new ResponseMyFurnitureDto();
                        dto.setId(estimate.getId());
                        dto.setFurniture2DUrl(estimate.getFurniture2DUrl());
                        dto.setFurnitureGlbUrl(estimate.getFurnitureGlbUrl());
                        dto.setFurnitureGltfUrl(estimate.getFurnitureGltfUrl());
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
            Estimate estimate = estimateRepository.findById(id).get();
            estimate.setNickName(null);
            log.info("[DELETE-MY-FURNITURE] deleting my furniture...");
            estimateRepository.delete(estimate);
            log.info("[DELETE-MY-FURNITURE] delete my furniture successfully!");
            return ResultCode.OK.toResponseEntity();
        }catch (Exception e){
            log.error("[DELETE-MY-FURNITURE] fail to delete my furniture!");
            e.printStackTrace();
            return ResultCode.FAIL.toResponseEntity();
        }
    }

    @Override
    public ResponseCompleteEstimate completeEstimate(EstimateChatContentsDto estimateChatContentsDto, String nickname) {
        log.info("[FINAL-ESTIMATE] updating estimate...");
        Estimate estimate = estimateRepository.findById(estimateChatContentsDto.getId()).get();
        if(estimate ==null){
            throw new NullPointerException();
        }
        try{
            estimate.setPrice(estimateChatContentsDto.getPrice());
            estimate.setDescription(estimateChatContentsDto.getDescription());
            estimate.setMakerName(nickname);

            estimateRepository.save(estimate);
            log.info("[FINAL-ESTIMATE] save estimate successfully!");
        }catch (Exception e){
            log.error("[FINAL-ESTIMATE] fail to save estimate!");
        }
        ResponseCompleteEstimate responseCompleteEstimate = new ResponseCompleteEstimate();
        responseCompleteEstimate.setId(estimate.getId());
        responseCompleteEstimate.setFurnitureName(estimate.getFurnitureName());
        responseCompleteEstimate.setFurniture3DUrl(estimate.getFurnitureGltfUrl());
        responseCompleteEstimate.setCreatedDate(estimate.getCreatedTime());
        responseCompleteEstimate.setPrice(estimateChatContentsDto.getPrice());
        return responseCompleteEstimate;
    }
}

package org.gagu.gagubackend.estimate.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.chat.dto.request.EstimateChatContentsDto;
import org.gagu.gagubackend.estimate.dao.EstimateDAO;
import org.gagu.gagubackend.estimate.dto.request.RequestSaveFurnitureDto;
import org.gagu.gagubackend.estimate.dto.response.ResponseCompleteEstimate;
import org.gagu.gagubackend.estimate.dto.response.ResponseMyFurnitureDto;
import org.gagu.gagubackend.estimate.service.EstimateService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EstimateServiceImpl implements EstimateService {
    private final EstimateDAO estimateDAO;
    @Override
    public ResponseEntity<?> saveFurniture(RequestSaveFurnitureDto requestSaveFurnitureDto, String nickname) {
        return estimateDAO.saveFurniture(requestSaveFurnitureDto, nickname);
    }

    @Override
    public Page<ResponseMyFurnitureDto> getFurniture(String nickname, Pageable pageable) {
        return estimateDAO.getMyFurniture(nickname, pageable);
    }

    @Override
    public Page<ResponseCompleteEstimate> getEstimate(String nickname, Pageable pageable) {
        return estimateDAO.getMyEstimates(nickname, pageable);
    }

    @Override
    public ResponseEntity<?> deleteFurniture(Long id) {
        return estimateDAO.deleteFurniture(id);
    }

    @Override
    public ResponseEntity<?> saveEstimate(EstimateChatContentsDto dto, String nickname) {
        return estimateDAO.completeEstimate(dto, nickname);
    }
}

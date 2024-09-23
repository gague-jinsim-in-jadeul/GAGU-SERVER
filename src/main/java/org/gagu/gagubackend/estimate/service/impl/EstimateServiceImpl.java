package org.gagu.gagubackend.estimate.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.estimate.dao.EstimateDAO;
import org.gagu.gagubackend.estimate.dto.request.RequestSaveFurnitureDto;
import org.gagu.gagubackend.estimate.service.EstimateService;
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
}

package org.gagu.gagubackend.estimate.service;

import org.gagu.gagubackend.estimate.dto.request.RequestSaveFurnitureDto;
import org.springframework.http.ResponseEntity;

public interface EstimateService {
    /**
     * 가구 2D, 3D 이미지 저장 서비스
     * @param requestSaveFurnitureDto
     * @param nickname
     * @return
     */
    ResponseEntity<?> saveFurniture(RequestSaveFurnitureDto requestSaveFurnitureDto, String nickname);

}

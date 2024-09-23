package org.gagu.gagubackend.estimate.dao;

import org.gagu.gagubackend.estimate.dto.request.RequestSaveFurnitureDto;
import org.springframework.http.ResponseEntity;

public interface EstimateDAO {
    /**
     * 가구 2D, 3D 이미지 저장 DAO
     * @param requestSaveFurnitureDto
     * @param nickname
     * @return
     */
    ResponseEntity<?> saveFurniture(RequestSaveFurnitureDto requestSaveFurnitureDto, String nickname);
}

package org.gagu.gagubackend.estimate.service;

import org.gagu.gagubackend.estimate.dto.request.RequestSaveFurnitureDto;
import org.gagu.gagubackend.estimate.dto.response.ResponseMyFurnitureDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface EstimateService {

    /**
     * 가구 2D, 3D 이미지 저장 서비스
     * @param requestSaveFurnitureDto
     * @param nickname
     * @return
     */
    ResponseEntity<?> saveFurniture(RequestSaveFurnitureDto requestSaveFurnitureDto, String nickname);
    /**
     * 사용자가 저장한 2D, 3D 이미지 반환
     * @param nickname
     * @return
     */
    Page<ResponseMyFurnitureDto> getFurniture(String nickname, Pageable pageable);

    /**
     * 사용자가 저장한 2D, 3D 이미지 삭제
     * @param id
     * @return
     */
    ResponseEntity<?> deleteFurniture(Long id);
}

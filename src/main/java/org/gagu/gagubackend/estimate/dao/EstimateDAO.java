package org.gagu.gagubackend.estimate.dao;

import org.gagu.gagubackend.chat.dto.request.EstimateChatContentsDto;
import org.gagu.gagubackend.estimate.dto.request.RequestSaveFurnitureDto;
import org.gagu.gagubackend.estimate.dto.response.ResponseCompleteEstimate;
import org.gagu.gagubackend.estimate.dto.response.ResponseMyFurnitureDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface EstimateDAO {
    /**
     * 가구 2D, 3D 이미지 저장 DAO
     * @param requestSaveFurnitureDto
     * @param nickname
     * @return
     */
    ResponseEntity<?> saveFurniture(RequestSaveFurnitureDto requestSaveFurnitureDto, String nickname);
    Page<ResponseMyFurnitureDto> getMyFurniture(String nickname, Pageable pageable);
    ResponseEntity<?> deleteFurniture(Long id);
    ResponseCompleteEstimate completeEstimate(EstimateChatContentsDto estimateChatContentsDto, String nickname);
}

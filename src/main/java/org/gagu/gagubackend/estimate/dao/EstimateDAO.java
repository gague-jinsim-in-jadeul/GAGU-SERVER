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

    /**
     * 저장한 가구 이미지 반환
     * @param nickname
     * @param pageable
     * @return
     */
    Page<ResponseMyFurnitureDto> getMyFurniture(String nickname, Pageable pageable);

    /**
     * 저장한 견적서 반환
     * @param nickname
     * @param pageable
     * @return
     */
    Page<ResponseCompleteEstimate> getMyEstimates(String nickname, Pageable pageable);

    /**
     * 공방관계자가 의뢰된 가구 이미지 조회
     * @param pageable
     * @param nickname
     * @param requester
     * @return
     */
    Page<ResponseMyFurnitureDto> getRequestFurnitures(Pageable pageable, String nickname, String requester);
    ResponseEntity<?> deleteFurniture(Long id);
    ResponseEntity<?> completeEstimate(EstimateChatContentsDto estimateChatContentsDto, String nickname);
}

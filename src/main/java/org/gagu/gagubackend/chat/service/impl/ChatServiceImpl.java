package org.gagu.gagubackend.chat.service.impl;


import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.chat.dao.ChatDAO;
import org.gagu.gagubackend.chat.domain.ChatContents;
import org.gagu.gagubackend.chat.dto.request.EstimateChatContentsDto;
import org.gagu.gagubackend.chat.dto.request.RequestChatContentsDto;
import org.gagu.gagubackend.chat.dto.request.RequestCreateChatRoomDto;
import org.gagu.gagubackend.chat.dto.response.ResponseChatDto;
import org.gagu.gagubackend.chat.dto.response.ResponseImageDto;
import org.gagu.gagubackend.chat.dto.response.ResponseMyChatRoomsDto;
import org.gagu.gagubackend.chat.service.ChatService;
import org.gagu.gagubackend.estimate.dao.EstimateDAO;
import org.gagu.gagubackend.user.dto.request.RequestUserInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ChatDAO chatDAO;
    private final EstimateDAO estimateDAO;
    private final AmazonS3Client amazonS3Client;
    @Value("${ai.host}")
    private String AI_HOST;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public ResponseEntity<?> createChatRoom(RequestUserInfoDto userInfoDto, RequestCreateChatRoomDto requestCreateChatRoomDto) {
        return chatDAO.createChatRoom(userInfoDto, requestCreateChatRoomDto);
    }

    @Override
    public ResponseEntity<?> exitChatRoom(Long roomNumber, String nickname) {
        return chatDAO.deleteChatRoom(roomNumber, nickname);
    }

    @Override
    public ResponseChatDto sendContents(RequestChatContentsDto message, Long roomNumber, String nickname) throws JsonProcessingException {
        String messageType = message.getType().toString();

        switch (messageType){
            case "SEND":
                return chatDAO.saveMessage(message,roomNumber,nickname);
            case "ESTIMATE":
                EstimateChatContentsDto estimateChatContentsDto = (EstimateChatContentsDto) message;
                return estimateDAO.completeEstimate(estimateChatContentsDto, nickname);
        }
        return null;
    }

    @Override
    public Page<ChatContents> getChatContents(String nickname, Pageable pageable, Long roomNumber) {
        return chatDAO.getChatContents(nickname,pageable,roomNumber);
    }

    @Override
    public Page<ResponseMyChatRoomsDto> getMyChatRooms(String nickname, Pageable pageable) {
        return chatDAO.getMyRooms(nickname, pageable);
    }

    @Override
    public ResponseImageDto generate2D(RequestChatContentsDto message) throws JsonProcessingException {
        String messageType = message.getType().toString();

        switch (messageType) {
            case "LLM":
                Map<String, String> requestBody = new HashMap<>();
                ObjectMapper objectMapper = new ObjectMapper();
                requestBody.put("prompt", message.getContents());
                log.info("[2D-LLM] prompt : {}", message.getContents());

                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders header = new HttpHeaders();
                header.setContentType(MediaType.APPLICATION_JSON); // JSON 형식으로 데이터 전달

                HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), header);
                try {
                    ResponseEntity<byte[]> response = restTemplate.postForEntity(AI_HOST, requestEntity, byte[].class);

                    if (response.getStatusCode() == HttpStatus.OK) {
                        log.info("[2D-LLM] success to generate 2d!");
                        byte[] imageBytes = response.getBody();

                        String filename = "2d-rendering" + System.currentTimeMillis()+".jpeg";
                        log.info(filename);

                        ObjectMetadata objectMetadata = new ObjectMetadata();
                        objectMetadata.setContentType("image/jpeg");
                        objectMetadata.setContentLength(imageBytes.length);

                        try{
                            //s3 업로드
                            log.info("[2D-LLM] uploading on s3..");
                            amazonS3Client.putObject(bucket, filename, new ByteArrayInputStream(imageBytes),objectMetadata);
                            log.info("[2D-LLM] success to upload s3!");
                        }catch (Exception e){
                            log.error("[2D-LLM] fail to upload s3!");
                            e.printStackTrace();
                        }


                        String url = amazonS3Client.getResourceUrl(bucket,filename).toString();
                        log.info("[2D-LLM] url : {}",url);
                            ResponseImageDto responseImageDto = ResponseImageDto.builder()
                                    .image(url)
                                    .dateTime(LocalDateTime.now())
                                    .build();

                            return responseImageDto;
                    }
                } catch (Exception e) {
                    log.error("[2D-LLM] fail to generate 2d!");
                    e.printStackTrace();
                }
        }
        return null;
    }
}

package org.gagu.gagubackend.chat.service.impl;


import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.chat.config.FCMConfig;
import org.gagu.gagubackend.chat.dao.ChatDAO;
import org.gagu.gagubackend.chat.domain.ChatContents;
import org.gagu.gagubackend.chat.dto.request.EstimateChatContentsDto;
import org.gagu.gagubackend.chat.dto.request.RequestChatContentsDto;
import org.gagu.gagubackend.chat.dto.request.RequestCreateChatRoomDto;
import org.gagu.gagubackend.chat.dto.request.RequestFCMSendDto;
import org.gagu.gagubackend.chat.dto.response.*;
import org.gagu.gagubackend.chat.service.ChatService;
import org.gagu.gagubackend.estimate.dao.EstimateDAO;
import org.gagu.gagubackend.auth.domain.User;
import org.gagu.gagubackend.auth.dto.request.RequestUserInfoDto;
import org.gagu.gagubackend.auth.repository.UserRepository;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ChatDAO chatDAO;
    private final EstimateDAO estimateDAO;
    private final AmazonS3Client amazonS3Client;
    private final UserRepository userRepository;
    private final FirebaseMessaging firebaseMessaging;
    @Value("${ai.host}")
    private String AI_HOST;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${firebase.secret.key}")
    private String FIREBASE_SECRET_KEY;
    @Value("${firebase.scope}")
    private String FIREBASE_SCOPE;
    @Value("${gagu.icon}")
    private String GAGU_ICON;
    @Value("${firebase.api.url}")
    private String FIREBASE_API_URL;

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
    public Page<ResponseChatContentsDto> getChatContents(String nickname, Pageable pageable, Long roomNumber) {
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

    @Override
    public ResponseEntity<?> sendMessageTo(RequestFCMSendDto requestFCMSendDto) {
        log.info("[CHATTING-NOTIFICATION] send to {}", requestFCMSendDto.getSenderNickname());

        User user = userRepository.findByNickName(requestFCMSendDto.getSenderNickname());
        String fcmToken = user.getFCMToken();

        log.info("[CHATTING-NOTIFICATION] fcm token : {}", fcmToken);

        try {
            Notification notification = Notification.builder()
                    .setTitle(requestFCMSendDto.getTitle())
                    .setBody(requestFCMSendDto.getBody())
                    .build();

            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification)
                    .build();

            firebaseMessaging.send(message);

            return ResultCode.OK.toResponseEntity();
        }catch (Exception e){
            e.printStackTrace();
            log.error("[CHATTING-NOTIFICATION] fail to send notification!");
            return ResultCode.FAIL.toResponseEntity();
        }
    }
}

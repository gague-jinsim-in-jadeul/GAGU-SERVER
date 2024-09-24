package org.gagu.gagubackend.chat.controller;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.aspose.threed.ObjSaveOptions;
import com.aspose.threed.Scene;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.chat.dto.request.RequestChatContentsDto;
import org.gagu.gagubackend.chat.dto.response.Response3DDto;
import org.gagu.gagubackend.chat.dto.response.ResponseImageDto;
import org.gagu.gagubackend.chat.service.ChatService;
import org.gagu.gagubackend.global.config.RedisConfig;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.gagu.gagubackend.global.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/v1/image")
@RequiredArgsConstructor
@Slf4j
public class ImageController {
    private final JwtTokenProvider jwtTokenProvider;
    private final AmazonS3Client amazonS3Client;
    private final ChatService chatService;
    private final SimpMessagingTemplate template;
    private final RedisConfig redisConfig;

    @Value("${stable.fast.3d}")
    private String STABLE_FAST_3D;
    @Value("${stable.diffustion.token}")
    private String STABLE_DIFFUSTION_TOKEN;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Operation(summary = "3D 랜더링", description = "사용자가 희망하는 2D 이미지를 3D 로 변환합니다.")
    @PostMapping("/3d")
    ResponseEntity<?> rendering3D(@RequestParam("file") MultipartFile file) throws IOException {

        if(file == null){
            log.error("[3D-rendering] no file!");
            return ResultCode.BAD_REQUEST.toResponseEntity();
        }

        log.info("[3D-rendering] 2D file : {}",file.getOriginalFilename());
        // ByteArrayResource 생성 시 getFilename() 메서드 오버라이드
        ByteArrayResource byteArrayResource = new ByteArrayResource(file.getBytes()){ // 익명 클래스 생성
            @Override
            public String getFilename() { // 인스턴스가 사용 될 때 메서드가 호출됨.
                return file.getOriginalFilename(); // 파일 이름 제공
            }
        };



        log.info("[3D-rendering] create 3D by 2D!");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", STABLE_DIFFUSTION_TOKEN);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", byteArrayResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try{
            log.info("[3D-rendering] rendering.......");
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    STABLE_FAST_3D,
                    HttpMethod.POST,
                    requestEntity,
                    byte[].class
            );

            if(response.getStatusCode() == HttpStatus.OK){ // 정상 응답 시
                log.info("[3D-rendering] rendering success!");

                // glb 임시 파일 저장
                String glbFileName = "3d-rendering" + System.currentTimeMillis() + ".glb";
                Files.write(Path.of(glbFileName), response.getBody());

                Scene scene = new Scene();
                ObjSaveOptions saveObjOpts = new ObjSaveOptions();
                saveObjOpts.setExportTextures(true);

                scene.open(glbFileName); // glb 터치
                String objFileName = "3d-rendering"+System.currentTimeMillis()+".obj";
                scene.save(objFileName, saveObjOpts);

                // glb 삭제
                try{
                    log.info("[3D-rendering] delete glb file!");
                    Files.delete(Path.of(glbFileName));
                }catch (Exception e){
                    log.error("[3D-rendering] fail to delete glb file!");
                    e.printStackTrace();
                }

                log.info("[3D-rendering] trying to upload 4 files on s3..");
                ObjectMetadata data = new ObjectMetadata(); // obj data
                data.setContentType("application/octet-stream"); // 파일 타입
                data.setContentLength(new File(objFileName).length()); // 파일 사이즈

                Response3DDto response3dDto = new Response3DDto();

                // obj upload
                try{
                    log.info("[3D-rendering] trying to upload obj file on s3..");
                    amazonS3Client.putObject(bucket,objFileName, new FileInputStream(objFileName),data);
                    log.info("[3D-rendering] success to upload obj file!");
                    String objUrl = amazonS3Client.getResourceUrl(bucket,objFileName);
                    response3dDto.setObjUrl(objUrl);
                    Files.delete(Path.of(objFileName));
                }catch (Exception e){
                    log.error("[3D-rendering] fail to upload obj file!");
                    e.printStackTrace();
                }

                // mtl upload
                try{
                    log.info("[3D-rendering] trying to upload mtl file on s3..");
                    data = new ObjectMetadata();
                    data.setContentType("text/plain");
                    String mtlFileName = objFileName.replace(".obj",".mtl");
                    data.setContentLength(new File(mtlFileName).length());
                    amazonS3Client.putObject(bucket,mtlFileName, new FileInputStream(mtlFileName),data);
                    String mtlUrl = amazonS3Client.getResourceUrl(bucket,mtlFileName);
                    response3dDto.setMtlUrl(mtlUrl);
                    Files.delete(Path.of(mtlFileName));
                }catch (Exception e){
                    log.error("[3D-rendering] fail to upload mtl file!");
                    e.printStackTrace();
                }
                try{
                    log.info("[3D-rendering] trying to upload texture_1 file on s3..");
                    data = new ObjectMetadata();
                    String texture_1 = "texture_1.jpg";
                    data.setContentType("image/jpeg");
                    data.setContentLength(new File(texture_1).length());
                    amazonS3Client.putObject(bucket,texture_1, new FileInputStream(texture_1),data);
                    String texture_1_Url = amazonS3Client.getResourceUrl(bucket,texture_1);
                    response3dDto.setTexture_1_Url(texture_1_Url);
                    Files.delete(Path.of(texture_1));
                }catch (Exception e) {
                    log.error("[3D-rendering] fail to upload texture_1 file!");
                    e.printStackTrace();
                }
                try{
                    log.info("[3D-rendering] trying to upload texture_2 file on s3..");
                    data = new ObjectMetadata();
                    String texture_2 = "texture_2.jpg";
                    data.setContentType("image/jpeg");
                    data.setContentLength(new File(texture_2).length());
                    amazonS3Client.putObject(bucket,texture_2, new FileInputStream(texture_2),data);
                    String texture_2_Url = amazonS3Client.getResourceUrl(bucket,texture_2);
                    response3dDto.setTexture_2_Url(texture_2_Url);
                    Files.delete(Path.of(texture_2));
                }catch (Exception e) {
                    log.error("[3D-rendering] fail to upload texture_1 file!");
                    e.printStackTrace();
                }

                return ResponseEntity.ok().body(response3dDto);

            }else{
                return ResultCode.FAIL.toResponseEntity();
            }
        }catch (Exception e){
            log.error("[3D-rendering] fail to rendering!");
            e.printStackTrace();
            return ResultCode.FAIL.toResponseEntity();
        }
    }
    @MessageMapping("/gagu-chat/2d") // mapping ex)/pub/gagu-chat/2d
    public void chattingWith2D(RequestChatContentsDto message,
                               SimpMessageHeaderAccessor accessor) throws Exception {
        log.info("[2D-chat] send prompt : {}", message.getContents());
        Thread.sleep(1000); // 비동기적으로 메시지를 처리하기 위해서 1초 지연(옵션)
        String nickname = (String) accessor.getSessionAttributes().get("senderNickname");
        ResponseImageDto responseChatDto = chatService.generate2D(message);
        template.convertAndSendToUser(nickname,"/sub", responseChatDto); // /user/{username}/{destination}
    }
}

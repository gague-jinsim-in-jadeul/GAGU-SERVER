package org.gagu.gagubackend.chat.controller;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.gagu.gagubackend.global.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/image")
@RequiredArgsConstructor
@Slf4j
public class ImageController {
    private final JwtTokenProvider jwtTokenProvider;
    private final AmazonS3Client amazonS3Client;

    @Value("${stable.fast.3d}")
    private String STABLE_FAST_3D;
    @Value("${stable.diffustion.token}")
    private String STABLE_DIFFUSTION_TOKEN;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @PostMapping("/3d")
    ResponseEntity<?> rendering3D(@RequestParam("file") MultipartFile file) throws IOException {

        if(file == null){
            log.error("[3D-rendering] no file!");
            return ResultCode.BAD_REQUEST.toResponseEntity();
        }

        log.info("[3D-rendering] 2D file : {}",file.getOriginalFilename());
        // ByteArrayResource 생성 시 getFilename() 메서드 오버라이드
        ByteArrayResource byteArrayResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
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
                String filename = "3d-rendering" + System.currentTimeMillis() + ".glb";
                ObjectMetadata data = new ObjectMetadata();

                data.setContentType("model/gltf-binary"); // 파일 타입
                data.setContentLength(response.getBody().length); // 파일 사이즈

                try{
                    log.info("[3D-rendering] try to upload on S3");
                    amazonS3Client.putObject(bucket, filename, new ByteArrayInputStream(response.getBody()), data);
                    log.info("[3D-rendering] successfully upload to S3!");
                    String glbUrl = amazonS3Client.getUrl(bucket,filename).toString();
                    return ResponseEntity.ok().body(Map.of("url", glbUrl));
                } catch (Exception e){
                    log.error("[3D-rendering] fail to upload on S3");
                    e.printStackTrace();
                }


            }else{
                return ResultCode.FAIL.toResponseEntity();
            }
        }catch (Exception e){
            log.error("[3D-rendering] fail to rendering!");
            e.printStackTrace();
            return ResultCode.FAIL.toResponseEntity();
        }
        return null;
    }
}

package org.gagu.gagubackend.global.controller;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.global.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/file")
@RequiredArgsConstructor
@Slf4j
public class FileController {
    private final AmazonS3Client amazonS3Client;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Operation(summary = "파일 업로드", description = "범용적으로 사용하는 파일 업로드 기능입니다.")
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file){

        log.info("[file-upload] file : {}",file.getOriginalFilename());
        try{
            String filename = file.getOriginalFilename();
            ObjectMetadata data = new ObjectMetadata();

            data.setContentType(file.getContentType()); // 파일 타입
            data.setContentLength(file.getSize()); // 파일 사이즈

            amazonS3Client.putObject(bucket, filename, file.getInputStream(), data);

            return ResponseEntity.ok(amazonS3Client.getUrl(bucket, file.getOriginalFilename()).toString());

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

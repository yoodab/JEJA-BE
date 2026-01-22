package com.jeja.jejabe.global.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileUploader {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 파일 업로드 및 저장된 파일 경로 반환
    public String upload(MultipartFile multipartFile, String dirName) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        // dirName에 해당하는 디렉토리가 없으면 생성
        File directory = new File(uploadDir + dirName);
        if (!directory.exists()) {
            directory.mkdirs(); // 하위 디렉토리까지 모두 생성
        }

        // 고유한 파일 이름 생성
        String originalFilename = multipartFile.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String savedFilename = UUID.randomUUID().toString() + extension;

        // 파일 저장
        String savedPath = Paths.get(uploadDir, dirName, savedFilename).toString();
        multipartFile.transferTo(new File(savedPath));

        // 웹에서 접근 가능한 상대 경로 반환 (예: /posts/uuid-filename.jpg)
        return "/" + dirName + "/" + savedFilename;
    }

    public void delete(String fileUrl) {
        try {
            // URL에서 실제 파일 경로 추출 (예: /files/uuid.jpg -> C:/dev/church-files/uuid.jpg)
            // 구현은 application.yml의 경로 설정에 따라 다름
            String fileName = fileUrl.replace("/files/", ""); // 가상 경로 제거
            Path filePath = Paths.get(uploadDir + fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", fileUrl, e);
        }
    }
}

package com.jeja.jejabe.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
public class FileService {

    @Value("${file.upload-dir}")
    private String fileDir; // 예: C:/uploads/ 또는 /app/uploads/ (마지막 슬래시 포함 여부 확인 필요)

    public String uploadFile(MultipartFile multipartFile, String folderName) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        // 1. 보안 체크: 폴더명에 ".."이 포함되어 상위 경로로 이동하는 것을 방지
        if (folderName.contains("..")) {
            throw new IllegalArgumentException("유효하지 않은 폴더명입니다.");
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String storedFileName = UUID.randomUUID().toString() + "_" + originalFilename;

        // 2. 저장 경로 생성 (기본경로 + 요청한폴더명)
        // fileDir가 "/"로 끝나는지 안 끝나는지에 따라 File.separator 처리 필요
        String folderPath = fileDir + (fileDir.endsWith(File.separator) ? "" : File.separator) + folderName;

        File directory = new File(folderPath);
        if (!directory.exists()) {
            directory.mkdirs(); // 해당 폴더가 없으면 생성 (예: uploads/members)
        }

        // 3. 파일 저장
        String fullPath = folderPath + File.separator + storedFileName;
        multipartFile.transferTo(new File(fullPath));

        // 4. 접근 URL 반환 (/files/폴더명/파일명)
        return "/files/" + folderName + "/" + storedFileName;
    }

    // Base64 저장 로직도 비슷하게 folderName을 받도록 확장 가능
    public String saveBase64File(String base64Data, String fileExtension, String folderName) throws IOException {
        if (folderName.contains("..")) {
            throw new IllegalArgumentException("유효하지 않은 폴더명입니다.");
        }

        byte[] imageBytes = Base64.getDecoder().decode(base64Data);
        String storedFileName = UUID.randomUUID().toString() + "." + fileExtension;

        String folderPath = fileDir + (fileDir.endsWith(File.separator) ? "" : File.separator) + folderName;
        File directory = new File(folderPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fullPath = folderPath + File.separator + storedFileName;

        try (FileOutputStream fos = new FileOutputStream(fullPath)) {
            fos.write(imageBytes);
        }

        return "/files/" + folderName + "/" + storedFileName;
    }
}
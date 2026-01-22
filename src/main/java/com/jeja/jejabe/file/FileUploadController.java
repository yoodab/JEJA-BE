package com.jeja.jejabe.file;

import com.jeja.jejabe.file.dto.FileUploadResponseDto;
import com.jeja.jejabe.global.response.ApiResponseForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseForm<FileUploadResponseDto>> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "common") String folder) throws IOException {

        // 1. 폴더명을 서비스로 전달
        String storedFileUrl = fileService.uploadFile(file, folder);

        // 2. 원본 파일명 추출
        String originalFilename = file.getOriginalFilename();

        // 3. DTO 생성
        FileUploadResponseDto responseDto = new FileUploadResponseDto(storedFileUrl, originalFilename);

        return ResponseEntity.ok(ApiResponseForm.success(responseDto, "파일 업로드 성공"));
    }
}

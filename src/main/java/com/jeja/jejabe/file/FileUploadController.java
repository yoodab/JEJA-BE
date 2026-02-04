package com.jeja.jejabe.file;

import com.jeja.jejabe.file.dto.FileUploadResponseDto;
import com.jeja.jejabe.global.response.ApiResponseForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseForm<List<FileUploadResponseDto>>> uploadFiles(
            @RequestPart("files") List<MultipartFile> files, // "file" -> "files" (복수형 권장)
            @RequestParam(value = "folder", defaultValue = "common") String folder) throws IOException {

        // 서비스에서 리스트 처리 후 DTO 리스트 반환
        List<FileUploadResponseDto> responseDtos = fileService.uploadFiles(files, folder);

        return ResponseEntity.ok(ApiResponseForm.success(responseDtos, "파일들이 업로드되었습니다."));
    }
}

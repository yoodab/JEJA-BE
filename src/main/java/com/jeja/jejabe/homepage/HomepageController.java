package com.jeja.jejabe.homepage;

import com.jeja.jejabe.global.response.ApiResponseForm;
import com.jeja.jejabe.homepage.dto.SlideReorderRequestDto;
import com.jeja.jejabe.homepage.dto.SlideRequestDto;
import com.jeja.jejabe.homepage.dto.YoutubeConfigRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HomepageController {

    private final HomepageService homepageService;

    // ================= 관리자 API (/api/admin/homepage) =================

    // 슬라이드 목록 조회 (관리자)
    @GetMapping("/api/admin/homepage/slides")
    public ResponseEntity<ApiResponseForm<List<Slide>>> getSlidesAdmin() {
        return ResponseEntity.ok(ApiResponseForm.success(homepageService.getAllSlides(), "슬라이드 목록 조회 성공"));
    }

    // 슬라이드 추가 (URL 방식)
    @PostMapping("/api/admin/homepage/slides")
    public ResponseEntity<ApiResponseForm<Long>> createSlide(@RequestBody SlideRequestDto request) {
        Long slideId = homepageService.createSlide(request);
        return ResponseEntity.ok(ApiResponseForm.success(slideId, "슬라이드 생성 성공"));
    }

    // 슬라이드 수정
    @PatchMapping("/api/admin/homepage/slides/{slideId}")
    public ResponseEntity<ApiResponseForm<Void>> updateSlide(
            @PathVariable Long slideId, @RequestBody SlideRequestDto request) {
        homepageService.updateSlide(slideId, request);
        return ResponseEntity.ok(ApiResponseForm.success("슬라이드 수정 성공"));
    }

    // 슬라이드 삭제
    @DeleteMapping("/api/admin/homepage/slides/{slideId}")
    public ResponseEntity<ApiResponseForm<Void>> deleteSlide(@PathVariable Long slideId) {
        homepageService.deleteSlide(slideId);
        return ResponseEntity.ok(ApiResponseForm.success("슬라이드 삭제 성공"));
    }

    // 슬라이드 순서 변경
    @PatchMapping("/api/admin/homepage/slides/reorder")
    public ResponseEntity<ApiResponseForm<Void>> reorderSlides(@RequestBody SlideReorderRequestDto request) {
        homepageService.reorderSlides(request.getSlideIds());
        return ResponseEntity.ok(ApiResponseForm.success("순서 변경 성공"));
    }

    // 유튜브 링크 조회 (관리자)
    @GetMapping("/api/admin/homepage/youtube-config")
    public ResponseEntity<ApiResponseForm<HomepageConfig>> getYoutubeAdmin() {
        return ResponseEntity.ok(ApiResponseForm.success(homepageService.getYoutubeConfig(), "설정 조회 성공"));
    }

    // 유튜브 링크 수정
    @PatchMapping("/api/admin/homepage/youtube-config")
    public ResponseEntity<ApiResponseForm<Void>> updateYoutube(@RequestBody YoutubeConfigRequestDto request) {
        homepageService.updateYoutubeConfig(request);
        return ResponseEntity.ok(ApiResponseForm.success("설정 수정 성공"));
    }

    // ================= 공개 API (/api/homepage) =================

    // 공개 슬라이드 목록 조회
    @GetMapping("/api/homepage/slides")
    public ResponseEntity<ApiResponseForm<List<Slide>>> getSlidesPublic() {
        return ResponseEntity.ok(ApiResponseForm.success(homepageService.getAllSlides(), "조회 성공"));
    }

    // 공개 유튜브 링크 조회
    @GetMapping("/api/homepage/youtube")
    public ResponseEntity<ApiResponseForm<HomepageConfig>> getYoutubePublic() {
        return ResponseEntity.ok(ApiResponseForm.success(homepageService.getYoutubeConfig(), "조회 성공"));
    }
}

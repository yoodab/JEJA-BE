package com.jeja.jejabe.newcomer;

import com.jeja.jejabe.global.response.ApiResponseForm;
import com.jeja.jejabe.newcomer.domain.NewcomerStatus;
import com.jeja.jejabe.newcomer.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/newcomers")
@RequiredArgsConstructor
@PreAuthorize("@newcomerGuard.canAccess(principal)")
public class NewcomerController {

    private final NewcomerService newcomerService;

    // 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponseForm<List<NewcomerListResponseDto>>> getNewcomers(
            @RequestParam(required = false) NewcomerStatus status) {
        return ResponseEntity.ok(ApiResponseForm.success(newcomerService.getNewcomerList(status)));
    }

    // 상세 조회
    @GetMapping("/{newcomerId}")
    public ResponseEntity<ApiResponseForm<NewcomerDetailResponseDto>> getNewcomer(@PathVariable Long newcomerId) {
        return ResponseEntity.ok(ApiResponseForm.success(newcomerService.getNewcomerDetails(newcomerId)));
    }

    // 등록 (이미지 URL 포함된 JSON)
    @PostMapping
    public ResponseEntity<ApiResponseForm<Long>> registerNewcomer(@RequestBody NewcomerCreateRequestDto requestDto) {
        Long id = newcomerService.registerNewcomer(requestDto);
        return ResponseEntity.ok(ApiResponseForm.success(id, "새신자가 등록되었습니다."));
    }

    // 수정 (이미지 URL 및 3단 상태 포함된 JSON)
    @PatchMapping("/{newcomerId}")
    public ResponseEntity<ApiResponseForm<Void>> updateNewcomer(
            @PathVariable Long newcomerId,
            @RequestBody NewcomerUpdateRequestDto requestDto) {
        newcomerService.updateNewcomer(newcomerId, requestDto);
        return ResponseEntity.ok(ApiResponseForm.success("새신자 정보가 수정되었습니다."));
    }

    // 등반 처리 (멤버로 승격)
    @PostMapping("/{newcomerId}/graduate")
    public ResponseEntity<ApiResponseForm<Long>> graduateNewcomer(@PathVariable Long newcomerId) {
        Long memberId = newcomerService.registerNewcomerAsMember(newcomerId);
        return ResponseEntity.ok(ApiResponseForm.success(memberId, "새신자가 정식 멤버로 등반했습니다."));
    }

    // 상태 변경 (중도포기 등)
    @PatchMapping("/{newcomerId}/status")
    public ResponseEntity<ApiResponseForm<Void>> changeStatus(
            @PathVariable Long newcomerId,
            @RequestParam NewcomerStatus status) {
        newcomerService.changeNewcomerStatus(newcomerId, status);
        return ResponseEntity.ok(ApiResponseForm.success("새신자 상태가 변경되었습니다."));
    }


}
package com.jeja.jejabe.care;

import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.care.dto.AbsenceCareDetailDto;
import com.jeja.jejabe.care.dto.AbsenceCareResponseDto;
import com.jeja.jejabe.care.dto.CareConfigDto;
import com.jeja.jejabe.care.dto.CareLogCreateRequestDto;
import com.jeja.jejabe.global.response.ApiResponseForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/care")
@RequiredArgsConstructor
public class CareController {

    private final CareService careService;

    @GetMapping("/absentees")
    public ResponseEntity<ApiResponseForm<List<AbsenceCareResponseDto>>> getAllAbsentees() {
        return ResponseEntity.ok(ApiResponseForm.success(careService.getAllAbsentees()));
    }

    @GetMapping("/absentees/{memberId}")
    public ResponseEntity<ApiResponseForm<AbsenceCareDetailDto>> getDetail(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponseForm.success(careService.getAbsenteeDetail(memberId)));
    }

    @PostMapping("/absentees/{memberId}/logs")
    public ResponseEntity<ApiResponseForm<Void>> addLog(
            @PathVariable Long memberId,
            @RequestBody CareLogCreateRequestDto dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        careService.addCareLog(memberId, dto, userDetails.getUser());
        return ResponseEntity.ok(ApiResponseForm.success("로그 저장 완료"));
    }

    @PatchMapping("/absentees/{memberId}/complete")
    public ResponseEntity<ApiResponseForm<Void>> completeCare(@PathVariable Long memberId) {
        careService.completeCare(memberId);
        return ResponseEntity.ok(ApiResponseForm.success("케어 완료 처리가 되었습니다."));
    }

    // [추가] 케어 로그 수정
    @PatchMapping("/absentees/{memberId}/logs/{logId}")
    public ResponseEntity<ApiResponseForm<Void>> updateCareLog(
            @PathVariable Long memberId,
            @PathVariable Long logId,
            @RequestBody CareLogCreateRequestDto dto) { // DTO 재활용 (내용, 방법)
        careService.updateCareLog(memberId, logId, dto);
        return ResponseEntity.ok(ApiResponseForm.success("로그가 수정되었습니다."));
    }

    // [추가] 케어 로그 삭제
    @DeleteMapping("/absentees/{memberId}/logs/{logId}")
    public ResponseEntity<ApiResponseForm<Void>> deleteCareLog(
            @PathVariable Long memberId,
            @PathVariable Long logId) {
        careService.deleteCareLog(memberId, logId);
        return ResponseEntity.ok(ApiResponseForm.success("로그가 삭제되었습니다."));
    }

    @GetMapping("/settings")
    public ResponseEntity<ApiResponseForm<CareConfigDto>> getConfig() {
        return ResponseEntity.ok(ApiResponseForm.success(careService.getConfig()));
    }

    @PutMapping("/settings")
    public ResponseEntity<ApiResponseForm<Void>> updateConfig(@RequestBody CareConfigDto dto) {
        careService.updateConfig(dto);
        return ResponseEntity.ok(ApiResponseForm.success("설정 저장 완료"));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponseForm<Map<String, Long>>> getCareSummary() {
        return ResponseEntity.ok(ApiResponseForm.success(careService.getCareSummary()));
    }

    @PatchMapping("/absentees/{memberId}/manager")
    public ResponseEntity<ApiResponseForm<Void>> updateManager(
            @PathVariable Long memberId,
            @RequestBody Long managerId) {
        careService.updateManager(memberId, managerId);
        return ResponseEntity.ok(ApiResponseForm.success("담당자 변경 완료"));
    }
}

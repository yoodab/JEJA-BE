package com.jeja.jejabe.form;

import com.jeja.jejabe.auth.User;
import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.form.dto.*;
import com.jeja.jejabe.global.response.ApiResponseForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FormController {

    private final FormService formService;

    @PostMapping("/forms/templates")
    public ResponseEntity<ApiResponseForm<Long>> createTemplate(@RequestBody TemplateCreateRequestDto dto) {
        return ResponseEntity.ok(ApiResponseForm.success(formService.createTemplate(dto)));
    }

    @PostMapping("/forms/submissions")
    public ResponseEntity<ApiResponseForm<Void>> submitForm(
            @RequestBody SubmissionRequestDto dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = (userDetails != null) ? userDetails.getUser() : null;
        formService.submitForm(dto, user);
        return ResponseEntity.ok(ApiResponseForm.success("제출 완료"));
    }

    @PatchMapping("/forms/submissions/{id}/approve")
    public ResponseEntity<ApiResponseForm<Void>> approveSubmission(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        formService.approveSubmission(id, userDetails.getUser());
        return ResponseEntity.ok(ApiResponseForm.success("승인 완료"));
    }

    @PatchMapping("/forms/submissions/{id}/reject")
    public ResponseEntity<ApiResponseForm<Void>> rejectSubmission(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        formService.rejectSubmission(id, userDetails.getUser());
        return ResponseEntity.ok(ApiResponseForm.success("거절 완료"));
    }

    @GetMapping("/forms/submissions/my")
    public ResponseEntity<ApiResponseForm<List<MySubmissionResponseDto>>> getMySubmissions(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponseForm.success(formService.getMySubmissions(userDetails.getUser())));
    }

    @GetMapping("/forms/submissions/last")
    public ResponseEntity<ApiResponseForm<SubmissionDetailResponseDto>> getLastSubmission(
            @RequestParam Long templateId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long cellId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = (userDetails != null) ? userDetails.getUser() : null;
        return ResponseEntity.ok(ApiResponseForm.success(formService.getLastSubmission(templateId, date, cellId, user)));
    }

    @GetMapping("/forms/submissions/{id}")
    public ResponseEntity<ApiResponseForm<SubmissionDetailResponseDto>> getSubmissionDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponseForm.success(formService.getSubmissionDetail(id, userDetails.getUser())));
    }

    @PutMapping("/forms/submissions/{id}")
    public ResponseEntity<ApiResponseForm<Void>> updateSubmission(
            @PathVariable Long id,
            @RequestBody SubmissionRequestDto dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = (userDetails != null) ? userDetails.getUser() : null;
        formService.updateSubmission(id, dto, user);
        return ResponseEntity.ok(ApiResponseForm.success("수정 완료"));
    }

    @GetMapping("/forms/templates/available")
    public ResponseEntity<ApiResponseForm<List<AvailableFormResponseDto>>> getAvailableForms(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponseForm.success(formService.getAvailableForms(userDetails.getUser())));
    }

    @GetMapping("/forms/templates/{id}")
    public ResponseEntity<ApiResponseForm<FormDetailResponseDto>> getFormDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = (userDetails != null) ? userDetails.getUser() : null;
        return ResponseEntity.ok(ApiResponseForm.success(formService.getTemplateDetail(id, user)));
    }

    @GetMapping("/forms/templates/club/{clubId}")
    public ResponseEntity<ApiResponseForm<FormDetailResponseDto>> getTemplateByClubId(
            @PathVariable Long clubId) {
        return ResponseEntity.ok(ApiResponseForm.success(formService.getTemplateByClubId(clubId)));
    }

    @GetMapping("/clubs/{clubId}/submissions")
    public ResponseEntity<ApiResponseForm<List<ClubSubmissionResponseDto>>> getClubSubmissions(
            @PathVariable Long clubId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity
                .ok(ApiResponseForm.success(formService.getClubApplications(clubId, userDetails.getUser())));
    }

    @PutMapping("/admin/forms/templates/{id}")
    public ResponseEntity<ApiResponseForm<Void>> updateTemplate(
            @PathVariable Long id,
            @RequestBody TemplateUpdateRequestDto dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        formService.updateTemplate(id, dto, userDetails.getUser());
        return ResponseEntity.ok(ApiResponseForm.success("수정 완료"));
    }

    @PatchMapping("/admin/forms/templates/{id}/status")
    public ResponseEntity<ApiResponseForm<Void>> updateTemplateStatus(
            @PathVariable Long id,
            @RequestBody FormStatusUpdateDto dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        // userDetails를 넘겨서 관리자 권한 체크를 Service에서 수행하도록 함
        formService.updateTemplateStatus(id, dto.getIsActive(), userDetails.getUser());

        return ResponseEntity.ok(ApiResponseForm.success("상태가 변경되었습니다."));
    }

    // 2. 아예 삭제 (목록에서 제거)
    @DeleteMapping("/admin/forms/templates/{id}")
    public ResponseEntity<ApiResponseForm<Void>> deleteTemplate(
            @PathVariable Long id) {
        formService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponseForm.success("삭제 완료"));
    }

    // --- [ADMIN] Template Management ---

    @GetMapping("/admin/forms/templates")
    public ResponseEntity<ApiResponseForm<List<AvailableFormResponseDto>>> getAllTemplates(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponseForm.success(
                formService.getAllTemplatesForAdmin(userDetails.getUser())));
    }

    @GetMapping("/admin/forms/templates/{id}")
    public ResponseEntity<ApiResponseForm<AdminFormDetailResponseDto>> getTemplateDetailForAdmin(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponseForm.success(
                formService.getTemplateDetailForAdmin(id, userDetails.getUser())));
    }
    // --- [ADMIN] Submission Management ---

    @GetMapping("/admin/forms/templates/{id}/submissions")
    public ResponseEntity<ApiResponseForm<List<AdminSubmissionSummaryDto>>> getSubmissions(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseForm.success(
                formService.getSubmissionsByTemplate(id)));
    }

    @GetMapping("/admin/forms/submissions/{id}")
    public ResponseEntity<ApiResponseForm<SubmissionDetailResponseDto>> getSubmissionDetail(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseForm.success(
                formService.getSubmissionDetailForAdmin(id)));
    }

}

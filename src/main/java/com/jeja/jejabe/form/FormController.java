package com.jeja.jejabe.form;

import com.jeja.jejabe.auth.User;
import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.form.dto.AvailableFormResponseDto;
import com.jeja.jejabe.form.dto.MySubmissionResponseDto;
import com.jeja.jejabe.form.dto.SubmissionRequestDto;
import com.jeja.jejabe.form.dto.TemplateCreateRequestDto;
import com.jeja.jejabe.global.response.ApiResponseForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FormController {

    private final FormService formService;

    @PostMapping("/admin/forms/templates")
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

    @GetMapping("/forms/submissions/my")
    public ResponseEntity<ApiResponseForm<List<MySubmissionResponseDto>>> getMySubmissions(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponseForm.success(formService.getMySubmissions(userDetails.getUser())));
    }

    @GetMapping("/forms/templates/available")
    public ResponseEntity<ApiResponseForm<List<AvailableFormResponseDto>>> getAvailableForms(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponseForm.success(formService.getAvailableForms(userDetails.getUser())));
    }
}

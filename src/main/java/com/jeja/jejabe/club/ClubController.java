package com.jeja.jejabe.club;

import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.club.dto.ClubCreateRequestDto;
import com.jeja.jejabe.club.dto.ClubDetailResponseDto;
import com.jeja.jejabe.club.dto.ClubResponseDto;
import com.jeja.jejabe.form.FormService;
import com.jeja.jejabe.form.dto.MySubmissionResponseDto;
import com.jeja.jejabe.global.response.ApiResponseForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    private final FormService formService;

    @GetMapping("/clubs/{clubId}/applications")
    public ResponseEntity<ApiResponseForm<List<MySubmissionResponseDto>>> getClubApplications(
            @PathVariable Long clubId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // 권한 체크 로직 (팀장 or 관리자) 필요 -> Service에서 수행
        return ResponseEntity.ok(ApiResponseForm.success(
                formService.getClubApplications(clubId, userDetails.getUser())
        ));
    }

    @GetMapping("/clubs")
    public ResponseEntity<ApiResponseForm<List<ClubResponseDto>>> getAllClubs() {
        return ResponseEntity.ok(ApiResponseForm.success(clubService.getAllClubs()));
    }

    @GetMapping("/clubs/my")
    public ResponseEntity<ApiResponseForm<List<ClubResponseDto>>> getMyClubs(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponseForm.success(clubService.getMyClubs(userDetails.getUser())));
    }

    @GetMapping("/clubs/{clubId}")
    public ResponseEntity<ApiResponseForm<ClubDetailResponseDto>> getClubDetail(@PathVariable Long clubId) {
        return ResponseEntity.ok(ApiResponseForm.success(clubService.getClubDetail(clubId)));
    }

    @PostMapping("/admin/clubs")
    public ResponseEntity<ApiResponseForm<Long>> createClub(@RequestBody ClubCreateRequestDto request) {
        return ResponseEntity.ok(ApiResponseForm.success(clubService.createClub(request)));
    }

    @DeleteMapping("/clubs/{clubId}/members/{memberId}")
    public ResponseEntity<ApiResponseForm<Void>> removeMember(
            @PathVariable Long clubId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        clubService.removeMember(clubId, memberId, userDetails.getUser());
        return ResponseEntity.ok(ApiResponseForm.success("멤버 퇴출 완료"));
    }

    @PatchMapping("/clubs/{clubId}/leader")
    public ResponseEntity<ApiResponseForm<Void>> changeLeader(
            @PathVariable Long clubId,
            @RequestBody Long newLeaderId, // 단순 ID 값 (JSON Body: 123)
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        clubService.changeLeader(clubId, newLeaderId, userDetails.getUser());
        return ResponseEntity.ok(ApiResponseForm.success("팀장 변경 완료"));
    }
}

package com.jeja.jejabe.club;

import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.club.dto.ClubCreateRequestDto;
import com.jeja.jejabe.club.dto.ClubDetailResponseDto;
import com.jeja.jejabe.club.dto.ClubLeaderChangeRequestDto;
import com.jeja.jejabe.club.dto.ClubMemberAddRequestDto;
import com.jeja.jejabe.club.dto.ClubResponseDto;
import com.jeja.jejabe.club.dto.ClubUpdateRequestDto;
import com.jeja.jejabe.form.FormService;
import com.jeja.jejabe.form.dto.ClubSubmissionResponseDto;
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
    public ResponseEntity<ApiResponseForm<List<ClubSubmissionResponseDto>>> getClubApplications(
            @PathVariable Long clubId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // 권한 체크 로직 (팀장 or 관리자) 필요 -> Service에서 수행
        return ResponseEntity.ok(ApiResponseForm.success(
                formService.getClubApplications(clubId, userDetails.getUser())));
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
            @RequestBody ClubLeaderChangeRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        clubService.changeLeader(clubId, requestDto.getNewLeaderId(), userDetails.getUser());
        return ResponseEntity.ok(ApiResponseForm.success("팀장 변경 완료"));
    }

    @PatchMapping("/clubs/{clubId}")
    public ResponseEntity<ApiResponseForm<Void>> updateClub(
            @PathVariable Long clubId,
            @RequestBody ClubUpdateRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        clubService.updateClub(clubId, requestDto, userDetails.getUser());
        return ResponseEntity.ok(ApiResponseForm.success(null));
    }

    @DeleteMapping("/admin/clubs/{clubId}")
    public ResponseEntity<ApiResponseForm<Void>> deleteClub(@PathVariable Long clubId) {
        clubService.deleteClub(clubId);
        return ResponseEntity.ok(ApiResponseForm.success(null));
    }

    @PostMapping("/clubs/{clubId}/members")
    public ResponseEntity<ApiResponseForm<Void>> addMember(
            @PathVariable Long clubId,
            @RequestBody ClubMemberAddRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        clubService.addMember(clubId, requestDto.getMemberId(), userDetails.getUser());
        return ResponseEntity.ok(ApiResponseForm.success(null));
    }

    @GetMapping("/clubs/types/{clubType}")
    public ResponseEntity<ApiResponseForm<ClubDetailResponseDto>> getClubByType(
            @PathVariable ClubType clubType) {
        return ResponseEntity.ok(ApiResponseForm.success(clubService.getClubByType(clubType)));
    }

}

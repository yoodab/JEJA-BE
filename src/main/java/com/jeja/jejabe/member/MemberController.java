package com.jeja.jejabe.member;

import com.jeja.jejabe.global.response.ApiResponseForm;
import com.jeja.jejabe.member.domain.MemberStatus;
import com.jeja.jejabe.member.dto.MemberCreateRequestDto;
import com.jeja.jejabe.member.dto.MemberDto;
import com.jeja.jejabe.member.dto.MemberStatisticsResponse;
import com.jeja.jejabe.member.dto.MemberUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/members") // 모든 경로는 관리자 권한 필요
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final ExcelMemberService excelMemberService;

    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR','EXECUTIVE')")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseForm<Integer>> importMembers(@RequestParam("file") MultipartFile file) {
        int uploadedCount = excelMemberService.uploadMembersFromExcel(file);
        return ResponseEntity.ok(ApiResponseForm.success(uploadedCount, uploadedCount + "명의 멤버가 성공적으로 등록되었습니다."));
    }

    @GetMapping
    public ResponseEntity<ApiResponseForm<Page<MemberDto>>> getMembers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) MemberStatus status,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(ApiResponseForm.success(memberService.getMembers(keyword, status, pageable)));
    }


    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponseForm<MemberDto>> getMemberById(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponseForm.success(memberService.getMemberById(memberId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseForm<Long>> createMember(@RequestBody MemberCreateRequestDto requestDto) {
        Long newMemberId = memberService.createMember(requestDto);
        return ResponseEntity.ok(ApiResponseForm.success(newMemberId, "새 멤버가 성공적으로 등록되었습니다."));
    }

    @PatchMapping("/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR','EXECUTIVE')")
    public ResponseEntity<ApiResponseForm<Void>> updateMember(@PathVariable Long memberId, @RequestBody MemberUpdateRequestDto requestDto) {
        memberService.updateMember(memberId, requestDto);
        return ResponseEntity.ok(ApiResponseForm.success("멤버 정보가 성공적으로 수정되었습니다."));
    }

    @DeleteMapping("/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR','EXECUTIVE')")
    public ResponseEntity<ApiResponseForm<Void>> deleteMember(@PathVariable Long memberId) {
        memberService.deleteMember(memberId);
        return ResponseEntity.ok(ApiResponseForm.success("멤버가 성공적으로 삭제되었습니다."));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponseForm<MemberStatisticsResponse>> getStatistics() {
        MemberStatisticsResponse response = memberService.getStatistics();
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }

    @GetMapping("/admin/unassigned") // URL: /api/members/admin/unassigned
    public ResponseEntity<ApiResponseForm<List<MemberDto>>> getUnassignedMembers() {
        List<MemberDto> unassignedMembers = memberService.getUnassignedMembers();
        return ResponseEntity.ok(ApiResponseForm.success(unassignedMembers));
    }
}

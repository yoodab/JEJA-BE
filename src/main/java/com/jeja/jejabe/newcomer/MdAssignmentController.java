package com.jeja.jejabe.newcomer;

import com.jeja.jejabe.global.response.ApiResponseForm;
import com.jeja.jejabe.newcomer.dto.MdAssignmentRequestDto;
import com.jeja.jejabe.newcomer.dto.MdAssignmentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/newcomers/mds") // URL 분리
@RequiredArgsConstructor
@PreAuthorize("@newcomerGuard.canAccess(principal)")
public class MdAssignmentController {

    private final MdAssignmentService mdService;

    @GetMapping
    public ResponseEntity<ApiResponseForm<List<MdAssignmentResponseDto>>> getMds() {
        return ResponseEntity.ok(ApiResponseForm.success(mdService.getAllMds()));
    }

    @PostMapping
    public ResponseEntity<ApiResponseForm<Long>> createMd(@RequestBody MdAssignmentRequestDto dto) {
        return ResponseEntity.ok(ApiResponseForm.success(mdService.createMd(dto)));
    }

    @PutMapping("/{mdId}")
    public ResponseEntity<ApiResponseForm<Void>> updateMd(@PathVariable Long mdId, @RequestBody MdAssignmentRequestDto dto) {
        mdService.updateMd(mdId, dto);
        return ResponseEntity.ok(ApiResponseForm.success(null));
    }

    @DeleteMapping("/{mdId}")
    public ResponseEntity<ApiResponseForm<Void>> deleteMd(@PathVariable Long mdId) {
        mdService.deleteMd(mdId);
        return ResponseEntity.ok(ApiResponseForm.success(null));
    }
}
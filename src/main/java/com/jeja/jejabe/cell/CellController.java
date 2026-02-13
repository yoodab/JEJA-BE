package com.jeja.jejabe.cell;

import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.cell.dto.*;
import com.jeja.jejabe.global.response.ApiResponseForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CellController {

    private final CellService cellService;

    // --- 일반 사용자용 API ---
    @GetMapping("/cells/my")
    public ResponseEntity<ApiResponseForm<MyCellResponseDto>> getMyCell(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        MyCellResponseDto myCellInfo = cellService.getMyCellInfo(userDetails.getUser());
        return ResponseEntity.ok(ApiResponseForm.success(myCellInfo, "자신이 속한 셀 정보 조회 성공"));
    }

    @GetMapping("/cells/{cellId}")
    public ResponseEntity<ApiResponseForm<CellDetailResponseDto>> getCellDetail(
            @PathVariable Long cellId) {
        return ResponseEntity.ok(ApiResponseForm.success(cellService.getCellDetail(cellId)));
    }

    // --- 관리자용 API ---
    @GetMapping("/admin/cells")
    public ResponseEntity<ApiResponseForm<List<CellDetailResponseDto>>> getAllCellsByYear(@RequestParam Integer year) {
        List<CellDetailResponseDto> cells = cellService.getAllCellsByYear(year);
        return ResponseEntity.ok(ApiResponseForm.success(cells));
    }

    @PostMapping("/admin/cells")
    public ResponseEntity<ApiResponseForm<Long>> createCell(@RequestBody CellCreateRequestDto requestDto) {
        Long newCellId = cellService.createCell(requestDto);
        return ResponseEntity.ok(ApiResponseForm.success(newCellId, "새로운 셀이 생성되었습니다."));
    }


    @PatchMapping("/admin/cells/{cellId}")
    public ResponseEntity<ApiResponseForm<Void>> updateCell(
            @PathVariable Long cellId,
            @RequestBody CellUpdateRequestDto requestDto) {
        cellService.updateCell(cellId, requestDto);
        return ResponseEntity.ok(ApiResponseForm.success("셀 정보가 수정되었습니다."));
    }

    @DeleteMapping("/admin/cells/{cellId}")
    public ResponseEntity<ApiResponseForm<Void>> deleteCell(@PathVariable Long cellId) {
        cellService.deleteCell(cellId);
        return ResponseEntity.ok(ApiResponseForm.success("셀이 삭제되었습니다."));
    }

    @PutMapping("/admin/cells/members/batch")
    public ResponseEntity<ApiResponseForm<Void>> updateCellMembersBatch(
            @RequestBody CellMemberBatchUpdateRequestDto requestDto) {

        cellService.updateCellMembersBatch(requestDto);
        return ResponseEntity.ok(ApiResponseForm.success("전체 순 구성원이 수정되었습니다."));
    }

    @PostMapping("/admin/activate")
    public ResponseEntity<ApiResponseForm<Void>> activateCells(@RequestParam Integer year) {
        cellService.activateCellsByYear(year);
        return ResponseEntity.ok(ApiResponseForm.success(year + "년도 순이 활성화되었습니다. (이전 연도 기록은 종료됨)"));
    }
}

package com.jeja.jejabe.finance.controller;

import com.jeja.jejabe.finance.domain.FinanceType;
import com.jeja.jejabe.finance.dto.CategoryDto;
import com.jeja.jejabe.finance.service.FinanceService;
import com.jeja.jejabe.global.response.ApiResponseForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finances/categories")
@RequiredArgsConstructor
public class FinanceCategoryController {

    private final FinanceService financeService;

    // 카테고리 목록 조회 (입력 화면 드롭다운용)
    @GetMapping
    public ResponseEntity<ApiResponseForm<List<CategoryDto>>> getCategories(@RequestParam FinanceType type) {
        return ResponseEntity.ok(ApiResponseForm.success(financeService.getCategories(type)));
    }

    // 2. 추가 (신규)
    @PostMapping
    public ResponseEntity<ApiResponseForm<Long>> createCategory(@RequestBody CategoryDto dto) {
        Long id = financeService.createCategory(dto.getName(), dto.getType());
        return ResponseEntity.ok(ApiResponseForm.success(id));
    }

    // 3. 수정 (신규)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseForm<Void>> updateCategory(@PathVariable Long id, @RequestBody CategoryDto dto) {
        // dto.getName()을 새 이름으로 사용
        financeService.updateCategory(id, dto.getName());
        return ResponseEntity.ok(ApiResponseForm.success(null));
    }

    // 4. 삭제 (신규)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseForm<Void>> deleteCategory(@PathVariable Long id) {
        financeService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponseForm.success(null));
    }
}

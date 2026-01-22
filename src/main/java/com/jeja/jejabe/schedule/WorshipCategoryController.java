package com.jeja.jejabe.schedule;

import com.jeja.jejabe.global.response.ApiResponseForm;
import com.jeja.jejabe.schedule.domain.WorshipCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/worship-categories")
@RequiredArgsConstructor
public class WorshipCategoryController {

    private final WorshipCategoryRepository repository;

    // 카테고리 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponseForm<List<WorshipCategory>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponseForm.success(repository.findAll()));
    }

    // 카테고리 생성 (예: "주일 3부 예배")
    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR')")
    @PostMapping
    public ResponseEntity<ApiResponseForm<WorshipCategory>> createCategory(@RequestBody String name) {
        // 실제로는 DTO를 쓰는 게 좋지만 간단하게 처리
        WorshipCategory category = repository.save(new WorshipCategory(name));
        return ResponseEntity.ok(ApiResponseForm.success(category));
    }

    // 카테고리 삭제
    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseForm<Void>> deleteCategory(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.ok(ApiResponseForm.success("삭제 완료"));
    }
}

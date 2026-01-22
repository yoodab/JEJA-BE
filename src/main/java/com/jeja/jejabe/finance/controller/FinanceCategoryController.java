package com.jeja.jejabe.finance.controller;

import com.jeja.jejabe.finance.domain.FinanceType;
import com.jeja.jejabe.finance.dto.CategoryDto;
import com.jeja.jejabe.finance.service.FinanceService;
import com.jeja.jejabe.global.response.ApiResponseForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}

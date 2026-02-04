package com.jeja.jejabe.finance.controller;

import com.jeja.jejabe.finance.domain.FinanceType;
import com.jeja.jejabe.finance.dto.CategoryDto;
import com.jeja.jejabe.finance.dto.FinanceRequestDto;
import com.jeja.jejabe.finance.dto.FinanceResponseDto;
import com.jeja.jejabe.finance.service.FinanceService;
import com.jeja.jejabe.global.response.ApiResponseForm;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/finances")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    @GetMapping
    public ResponseEntity<ApiResponseForm<List<FinanceResponseDto>>> getFinances(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        if (endDate == null) endDate = LocalDate.now();
        return ResponseEntity.ok(ApiResponseForm.success(financeService.getFinanceList(startDate, endDate)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseForm<Void>> createFinance(@RequestBody FinanceRequestDto dto) {
        financeService.createFinance(dto);
        return ResponseEntity.ok(ApiResponseForm.success(null));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponseForm<Void>> createFinanceBatch(@RequestBody List<FinanceRequestDto> dtos) {
        financeService.createFinanceBatch(dtos);
        return ResponseEntity.ok(ApiResponseForm.success(null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseForm<Void>> updateFinance(@PathVariable Long id, @RequestBody FinanceRequestDto dto) {
        financeService.updateFinance(id, dto);
        return ResponseEntity.ok(ApiResponseForm.success(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseForm<Void>> deleteFinance(@PathVariable Long id) {
        financeService.deleteFinance(id);
        return ResponseEntity.ok(ApiResponseForm.success(null));
    }



}
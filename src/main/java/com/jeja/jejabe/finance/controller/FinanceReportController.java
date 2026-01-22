package com.jeja.jejabe.finance.controller;

import com.jeja.jejabe.finance.dto.YearlyReportDto;
import com.jeja.jejabe.finance.service.FinanceReportService;
import com.jeja.jejabe.global.response.ApiResponseForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finances/report")
@RequiredArgsConstructor
public class FinanceReportController {

    private final FinanceReportService financeReportService;

    @GetMapping("/yearly")
    public ResponseEntity<ApiResponseForm<YearlyReportDto>> getYearlyReport(@RequestParam int year) {
        return ResponseEntity.ok(ApiResponseForm.success(financeReportService.getYearlyReport(year)));
    }
}

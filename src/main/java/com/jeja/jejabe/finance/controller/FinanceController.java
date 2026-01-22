package com.jeja.jejabe.finance.controller;

import com.jeja.jejabe.finance.dto.FinanceRequestDto;
import com.jeja.jejabe.finance.dto.FinanceResponseDto;
import com.jeja.jejabe.finance.service.FinanceService;
import com.jeja.jejabe.global.response.ApiResponseForm;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
        return ResponseEntity.ok(ApiResponseForm.success("저장 완료"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseForm<Void>> deleteFinance(@PathVariable Long id) {
        financeService.deleteFinance(id);
        return ResponseEntity.ok(ApiResponseForm.success("삭제 완료"));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseForm<Integer>> uploadExcel(@RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(ApiResponseForm.success(financeService.uploadExcel(file), "업로드 완료"));
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> downloadExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        ByteArrayInputStream in = financeService.downloadExcel(startDate, endDate);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=account_book.xlsx");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM).body(new InputStreamResource(in));
    }
}

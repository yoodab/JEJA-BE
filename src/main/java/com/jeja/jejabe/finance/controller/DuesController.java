package com.jeja.jejabe.finance.controller;

import com.jeja.jejabe.finance.dto.DuesEventDto;
import com.jeja.jejabe.finance.dto.DuesRecordDto;
import com.jeja.jejabe.finance.service.DuesService;
import com.jeja.jejabe.global.response.ApiResponseForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dues")
@RequiredArgsConstructor
public class DuesController {

    private final DuesService duesService;

    @GetMapping("/events")
    public ResponseEntity<ApiResponseForm<List<DuesEventDto>>> getEvents() {
        return ResponseEntity.ok(ApiResponseForm.success(duesService.getEvents()));
    }

    @PostMapping("/events")
    public ResponseEntity<ApiResponseForm<Long>> createEvent(@RequestBody DuesEventDto dto) {
        return ResponseEntity.ok(ApiResponseForm.success(duesService.createEvent(dto)));
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<ApiResponseForm<Void>> updateEvent(@PathVariable Long id, @RequestBody DuesEventDto dto) {
        duesService.updateEvent(id, dto);
        return ResponseEntity.ok(ApiResponseForm.success(null));
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<ApiResponseForm<Void>> deleteEvent(@PathVariable Long id) {
        duesService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponseForm.success(null));
    }

    @GetMapping("/records")
    public ResponseEntity<ApiResponseForm<List<DuesRecordDto>>> getRecords(@RequestParam Long eventId) {
        return ResponseEntity.ok(ApiResponseForm.success(duesService.getRecords(eventId)));
    }

    @PostMapping("/records/batch")
    public ResponseEntity<ApiResponseForm<Void>> addRecordsBatch(@RequestBody List<DuesRecordDto> dtos) {
        duesService.createRecordsBatch(dtos);
        return ResponseEntity.ok(ApiResponseForm.success(null));
    }

    @PutMapping("/records/{id}")
    public ResponseEntity<ApiResponseForm<Void>> updateRecord(@PathVariable Long id, @RequestBody DuesRecordDto dto) {
        duesService.updateRecord(id, dto);
        return ResponseEntity.ok(ApiResponseForm.success(null));
    }

    @DeleteMapping("/records/{id}")
    public ResponseEntity<ApiResponseForm<Void>> deleteRecord(@PathVariable Long id) {
        duesService.deleteRecord(id);
        return ResponseEntity.ok(ApiResponseForm.success(null));
    }
}

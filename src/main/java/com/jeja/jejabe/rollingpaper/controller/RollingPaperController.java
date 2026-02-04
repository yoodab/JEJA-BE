package com.jeja.jejabe.rollingpaper.controller;

import com.jeja.jejabe.rollingpaper.dto.MessageCreateRequestDto;
import com.jeja.jejabe.rollingpaper.dto.RollingPaperCreateRequestDto;
import com.jeja.jejabe.rollingpaper.dto.RollingPaperResponseDto;
import com.jeja.jejabe.rollingpaper.dto.StickerCreateRequestDto;
import com.jeja.jejabe.rollingpaper.service.RollingPaperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rolling-papers")
public class RollingPaperController {

    private final RollingPaperService rollingPaperService;

    // Admin: Create
    @PostMapping
    public ResponseEntity<Long> createRollingPaper(@RequestBody RollingPaperCreateRequestDto requestDto) {
        return ResponseEntity.ok(rollingPaperService.createRollingPaper(requestDto));
    }

    // Admin: List
    @GetMapping
    public ResponseEntity<List<RollingPaperResponseDto>> getAllRollingPapers() {
        return ResponseEntity.ok(rollingPaperService.getAllRollingPapers());
    }

    // Public: Detail
    @GetMapping("/{id}")
    public ResponseEntity<RollingPaperResponseDto> getRollingPaper(@PathVariable Long id) {
        return ResponseEntity.ok(rollingPaperService.getRollingPaper(id));
    }

    // Public: Add Message
    @PostMapping("/{id}/messages")
    public ResponseEntity<Long> addMessage(@PathVariable Long id, @RequestBody MessageCreateRequestDto requestDto) {
        return ResponseEntity.ok(rollingPaperService.addMessage(id, requestDto));
    }

    // Public: Add Sticker
    @PostMapping("/{id}/stickers")
    public ResponseEntity<Long> addSticker(@PathVariable Long id, @RequestBody StickerCreateRequestDto requestDto) {
        return ResponseEntity.ok(rollingPaperService.addSticker(id, requestDto));
    }

    // Admin: Update
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateRollingPaper(@PathVariable Long id, @RequestBody RollingPaperCreateRequestDto requestDto) {
        rollingPaperService.updateRollingPaper(id, requestDto);
        return ResponseEntity.ok().build();
    }

    // Admin: Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRollingPaper(@PathVariable Long id) {
        rollingPaperService.deleteRollingPaper(id);
        return ResponseEntity.ok().build();
    }
}

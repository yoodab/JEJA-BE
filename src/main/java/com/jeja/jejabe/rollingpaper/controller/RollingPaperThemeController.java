package com.jeja.jejabe.rollingpaper.controller;

import com.jeja.jejabe.rollingpaper.domain.RollingPaperTheme;
import com.jeja.jejabe.rollingpaper.service.RollingPaperThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rolling-papers/themes")
@RequiredArgsConstructor
public class RollingPaperThemeController {
    private final RollingPaperThemeService themeService;

    @GetMapping
    public List<RollingPaperTheme> getAllThemes() {
        return themeService.getAllThemes();
    }

    @PostMapping
    public RollingPaperTheme createTheme(@RequestBody Map<String, String> request) {
        return themeService.createTheme(request.get("name"), request.get("themeConfig"));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long id) {
        themeService.deleteTheme(id);
        return ResponseEntity.ok().build();
    }
}

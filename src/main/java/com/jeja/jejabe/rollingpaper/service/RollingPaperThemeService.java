package com.jeja.jejabe.rollingpaper.service;

import com.jeja.jejabe.rollingpaper.domain.RollingPaperTheme;
import com.jeja.jejabe.rollingpaper.repository.RollingPaperThemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RollingPaperThemeService {
    private final RollingPaperThemeRepository themeRepository;

    public List<RollingPaperTheme> getAllThemes() {
        return themeRepository.findAll();
    }

    @Transactional
    public RollingPaperTheme createTheme(String name, String config) {
        RollingPaperTheme theme = new RollingPaperTheme();
        theme.setName(name);
        theme.setThemeConfig(config);
        return themeRepository.save(theme);
    }
    
    @Transactional
    public void deleteTheme(Long id) {
        themeRepository.deleteById(id);
    }
}

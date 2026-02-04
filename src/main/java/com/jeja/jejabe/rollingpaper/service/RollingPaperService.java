package com.jeja.jejabe.rollingpaper.service;

import com.jeja.jejabe.rollingpaper.domain.RollingPaper;
import com.jeja.jejabe.rollingpaper.domain.RollingPaperMessage;
import com.jeja.jejabe.rollingpaper.domain.RollingPaperSticker;
import com.jeja.jejabe.rollingpaper.dto.MessageCreateRequestDto;
import com.jeja.jejabe.rollingpaper.dto.RollingPaperCreateRequestDto;
import com.jeja.jejabe.rollingpaper.dto.RollingPaperResponseDto;
import com.jeja.jejabe.rollingpaper.dto.StickerCreateRequestDto;
import com.jeja.jejabe.rollingpaper.repository.RollingPaperMessageRepository;
import com.jeja.jejabe.rollingpaper.repository.RollingPaperRepository;
import com.jeja.jejabe.rollingpaper.repository.RollingPaperStickerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RollingPaperService {

    private final RollingPaperRepository rollingPaperRepository;
    private final RollingPaperMessageRepository messageRepository;
    private final RollingPaperStickerRepository stickerRepository;

    @Transactional
    public Long createRollingPaper(RollingPaperCreateRequestDto requestDto) {
        return rollingPaperRepository.save(requestDto.toEntity()).getId();
    }

    @Transactional(readOnly = true)
    public List<RollingPaperResponseDto> getAllRollingPapers() {
        return rollingPaperRepository.findAll().stream()
                .map(RollingPaperResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RollingPaperResponseDto getRollingPaper(Long id) {
        RollingPaper rollingPaper = rollingPaperRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("RollingPaper not found with id: " + id));
        return new RollingPaperResponseDto(rollingPaper);
    }

    @Transactional
    public Long addMessage(Long rollingPaperId, MessageCreateRequestDto requestDto) {
        RollingPaper rollingPaper = rollingPaperRepository.findById(rollingPaperId)
                .orElseThrow(() -> new IllegalArgumentException("RollingPaper not found with id: " + rollingPaperId));
        return messageRepository.save(requestDto.toEntity(rollingPaper)).getId();
    }

    @Transactional
    public Long addSticker(Long rollingPaperId, StickerCreateRequestDto requestDto) {
        RollingPaper rollingPaper = rollingPaperRepository.findById(rollingPaperId)
                .orElseThrow(() -> new IllegalArgumentException("RollingPaper not found with id: " + rollingPaperId));
        return stickerRepository.save(requestDto.toEntity(rollingPaper)).getId();
    }

    @Transactional
    public void updateRollingPaper(Long id, RollingPaperCreateRequestDto requestDto) {
        RollingPaper rollingPaper = rollingPaperRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("RollingPaper not found with id: " + id));
        rollingPaper.update(requestDto.getTitle(), requestDto.getTheme(), requestDto.getBackgroundConfig());
    }

    @Transactional
    public void deleteRollingPaper(Long id) {
        RollingPaper rollingPaper = rollingPaperRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("RollingPaper not found with id: " + id));
        rollingPaperRepository.delete(rollingPaper);
    }
}

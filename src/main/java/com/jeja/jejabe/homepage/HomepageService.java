package com.jeja.jejabe.homepage;

import com.jeja.jejabe.homepage.dto.SlideRequestDto;
import com.jeja.jejabe.homepage.dto.TextElementDto;
import com.jeja.jejabe.homepage.dto.YoutubeConfigRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HomepageService {

    private final SlideRepository slideRepository;
    private final HomepageConfigRepository configRepository;

    // --- 슬라이드 관련 ---

    @Transactional(readOnly = true)
    public List<Slide> getAllSlides() {
        return slideRepository.findAllByOrderByOrderIndexAsc();
    }

    public Long createSlide(SlideRequestDto dto) {
        Slide slide = new Slide();
        // DTO -> Entity 매핑
        updateSlideFromDto(slide, dto);

        // 순서 지정 (맨 뒤에 추가)
        Integer maxOrder = slideRepository.findMaxOrderIndex();
        slide.setOrderIndex(maxOrder == null ? 1 : maxOrder + 1);

        return slideRepository.save(slide).getId();
    }

    public void updateSlide(Long slideId, SlideRequestDto dto) {
        Slide slide = slideRepository.findById(slideId)
                .orElseThrow(() -> new IllegalArgumentException("Slide not found"));
        updateSlideFromDto(slide, dto);
    }

    public void deleteSlide(Long slideId) {
        slideRepository.deleteById(slideId);
    }

    public void reorderSlides(List<Long> slideIds) {
        // 프론트에서 보내준 ID 순서대로 인덱스 재지정
        for (int i = 0; i < slideIds.size(); i++) {
            Long id = slideIds.get(i);
            Slide slide = slideRepository.findById(id).orElseThrow();
            slide.setOrderIndex(i + 1);
        }
    }

    // 공통 매핑 로직
    private void updateSlideFromDto(Slide slide, SlideRequestDto dto) {
        slide.setType(SlideType.valueOf(dto.getType().toUpperCase()));

        if ("image".equalsIgnoreCase(dto.getType())) {
            // ★ 중요: 프론트에서 받은 URL 문자열을 그대로 저장
            slide.setUrl(dto.getUrl());
            slide.setLinkUrl(dto.getLinkUrl());
            slide.setTitle(dto.getTitle());
            slide.setBackgroundColor(dto.getBackgroundColor());
            // 이미지 타입으로 변경 시 텍스트 요소 제거
            slide.getTextElements().clear();
        } else {
            // 텍스트 슬라이드 설정
            slide.setBackgroundColor(dto.getBackgroundColor());
            slide.setLinkUrl(dto.getLinkUrl());
            slide.setTitle(dto.getTitle());
            // 기존 텍스트 요소 싹 지우고 새로 등록 (orphanRemoval 동작)
            slide.getTextElements().clear();
            if (dto.getTextElements() != null) {
                for (TextElementDto elemDto : dto.getTextElements()) {
                    SlideTextElement element = new SlideTextElement();
                    element.setElementId(elemDto.getId());
                    element.setText(elemDto.getText());
                    element.setFontSize(elemDto.getFontSize());
                    element.setColor(elemDto.getColor());
                    element.setX(elemDto.getX());
                    element.setY(elemDto.getY());
                    element.setFontWeight(elemDto.getFontWeight());
                    element.setFontFamily(elemDto.getFontFamily());

                    slide.addTextElement(element); // 연관관계 설정 포함됨
                }
            }
        }
    }

    // --- 유튜브 설정 관련 ---

    @Transactional(readOnly = true)
    public HomepageConfig getYoutubeConfig() {
        // 없으면 빈 객체 반환 (기본값)
        return configRepository.findById(1L).orElse(new HomepageConfig());
    }

    public void updateYoutubeConfig(YoutubeConfigRequestDto dto) {
        HomepageConfig config = configRepository.findById(1L).orElse(new HomepageConfig());
        config.setId(1L); // ID 보장
        config.setLiveUrl(dto.getLiveUrl());
        config.setPlaylistUrl(dto.getPlaylistUrl());
        configRepository.save(config);
    }
}
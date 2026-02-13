package com.jeja.jejabe.homepage;

import com.jeja.jejabe.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Slide extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private SlideType type; // TEXT, IMAGE

    private Integer orderIndex; // 슬라이드 순서

    // --- 이미지 슬라이드용 필드 ---
    private String url;       // 이미지 URL (FileController에서 받은 값)
    private String linkUrl;   // 클릭 시 이동할 링크
    private String title;

    // --- 텍스트 슬라이드용 필드 ---
    private String backgroundColor;

    // 텍스트 슬라이드는 여러 개의 텍스트 요소를 가짐
    @OneToMany(mappedBy = "slide", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SlideTextElement> textElements = new ArrayList<>();

    // 연관관계 편의 메소드
    public void addTextElement(SlideTextElement element) {
        this.textElements.add(element);
        element.setSlide(this);
    }
}

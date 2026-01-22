package com.jeja.jejabe.homepage;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SlideTextElement {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    private Integer fontSize;
    private String color;
    private Double x; // 위치 X (%)
    private Double y; // 위치 Y (%)
    private String fontWeight;
    private String fontFamily;
    private String elementId; // 프론트엔드에서 관리하는 식별자 (예: text-1234)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slide_id")
    private Slide slide;
}

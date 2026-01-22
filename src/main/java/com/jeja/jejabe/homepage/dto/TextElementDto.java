package com.jeja.jejabe.homepage.dto;

import lombok.Data;

@Data
public class TextElementDto {
    private String id; // elementId
    private String text;
    private Integer fontSize;
    private String color;
    private Double x;
    private Double y;
    private String fontWeight;
    private String fontFamily;
}

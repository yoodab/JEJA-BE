package com.jeja.jejabe.homepage;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class HomepageConfig {
    @Id
    private Long id = 1L; // 설정값은 단 1줄만 존재 (ID: 1 고정)

    private String liveUrl;
    private String playlistUrl;
}

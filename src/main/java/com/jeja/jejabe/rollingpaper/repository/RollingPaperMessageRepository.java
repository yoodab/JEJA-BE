package com.jeja.jejabe.rollingpaper.repository;

import com.jeja.jejabe.rollingpaper.domain.RollingPaperMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RollingPaperMessageRepository extends JpaRepository<RollingPaperMessage, Long> {
}

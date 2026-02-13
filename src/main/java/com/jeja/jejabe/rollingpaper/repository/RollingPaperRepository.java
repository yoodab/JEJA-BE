package com.jeja.jejabe.rollingpaper.repository;

import com.jeja.jejabe.rollingpaper.domain.RollingPaper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RollingPaperRepository extends JpaRepository<RollingPaper, Long> {
    Page<RollingPaper> findByTitleContaining(String title, Pageable pageable);
}

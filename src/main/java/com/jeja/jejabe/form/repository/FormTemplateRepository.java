package com.jeja.jejabe.form.repository;

import com.jeja.jejabe.form.domain.FormCategory;
import com.jeja.jejabe.form.domain.FormTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FormTemplateRepository extends JpaRepository<FormTemplate, Long> {
    // 동아리용 템플릿 찾기
    Optional<FormTemplate> findByTargetClubId(Long clubId);

    // 카테고리별 템플릿 찾기 (예: 모든 순보고서 양식)
    List<FormTemplate> findAllByCategory(FormCategory category);
}

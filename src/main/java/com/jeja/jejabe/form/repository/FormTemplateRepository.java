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

    List<FormTemplate> findAllByOrderByCreatedAtDesc();

    // [추가] 클럽 전용이 아닌 '일반 폼'만 조회 (targetClubId IS NULL)
    List<FormTemplate> findAllByTargetClubIdIsNullOrderByCreatedAtDesc();

    // [관리자용] 전체 조회 (삭제된 것 제외, 클럽용 제외)
    List<FormTemplate> findAllByTargetClubIdIsNullAndIsDeletedFalseOrderByCreatedAtDesc();

    // [사용자용] 작성 가능 목록 조회 (삭제된 것 제외, 전체)
    List<FormTemplate> findAllByIsDeletedFalse();
}

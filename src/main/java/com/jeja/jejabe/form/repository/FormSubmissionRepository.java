package com.jeja.jejabe.form.repository;

import com.jeja.jejabe.form.domain.FormSubmission;
import com.jeja.jejabe.form.domain.FormTemplate;
import com.jeja.jejabe.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FormSubmissionRepository extends JpaRepository<FormSubmission, Long> {
    List<FormSubmission> findAllByTargetClubId(Long clubId); // 동아리 신청서 목록
    List<FormSubmission> findAllByTargetCellId(Long cellId); // 순 보고서 목록
    // 중복 제출 확인용
    boolean existsBySubmitterAndSubmitDateAndTargetCellId(Member submitter, LocalDate date, Long cellId);
    // 순 보고서 중복 확인
    boolean existsBySubmitterAndTargetSundayDateAndTargetCellId(Member submitter, LocalDate targetSundayDate, Long cellId);
    // 내가 낸 것 목록
    List<FormSubmission> findAllBySubmitterOrderBySubmitDateDesc(Member submitter);
    List<FormSubmission> findAllByTemplateOrderByCreatedAtDesc(FormTemplate template);
    // 특정 폼 제출 여부 확인용
    Optional<FormSubmission> findFirstByTemplateAndSubmitterOrderBySubmitDateDesc(FormTemplate template, Member submitter);
}

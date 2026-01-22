package com.jeja.jejabe.form.repository;

import com.jeja.jejabe.form.domain.FormQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormQuestionRepository extends JpaRepository<FormQuestion, Long> {
}

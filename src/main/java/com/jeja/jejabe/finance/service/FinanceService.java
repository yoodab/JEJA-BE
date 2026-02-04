package com.jeja.jejabe.finance.service;

import com.jeja.jejabe.finance.repository.FinanceCategoryRepository;
import com.jeja.jejabe.finance.repository.FinanceRepository;
import com.jeja.jejabe.finance.domain.Finance;
import com.jeja.jejabe.finance.domain.FinanceCategory;
import com.jeja.jejabe.finance.domain.FinanceType;
import com.jeja.jejabe.finance.dto.CategoryDto;
import com.jeja.jejabe.finance.dto.FinanceRequestDto;
import com.jeja.jejabe.finance.dto.FinanceResponseDto;
import com.jeja.jejabe.schedule.domain.Schedule;
import com.jeja.jejabe.schedule.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FinanceService {

    private final FinanceRepository financeRepository;
    private final FinanceCategoryRepository categoryRepository;
    private final ScheduleRepository scheduleRepository;

    @Transactional(readOnly = true)
    public List<FinanceResponseDto> getFinanceList(LocalDate startDate, LocalDate endDate) {
        List<Finance> records = financeRepository.findAllByTransactionDateBetweenOrderByTransactionDateAsc(startDate, endDate);

        long currentBalance = 0L;
        List<FinanceResponseDto> result = new ArrayList<>();
        for (Finance f : records) {
            if (f.getType() == FinanceType.INCOME) currentBalance += f.getAmount();
            else currentBalance -= f.getAmount();

            result.add(new FinanceResponseDto(f, currentBalance));
        }
        return result;
    }

    public void createFinance(FinanceRequestDto dto) {
        saveFinanceFromDto(dto);
    }

    public void createFinanceBatch(List<FinanceRequestDto> dtos) {
        for (FinanceRequestDto dto : dtos) {
            saveFinanceFromDto(dto);
        }
    }

    private void saveFinanceFromDto(FinanceRequestDto dto) {
        Schedule schedule = null;
        if (dto.getScheduleId() != null) {
            schedule = scheduleRepository.findById(dto.getScheduleId()).orElse(null);
        }

        FinanceCategory category = categoryRepository.findByNameAndType(dto.getCategory(), dto.getTransactionType())
                .orElseGet(() -> categoryRepository.save(new FinanceCategory(dto.getCategory(), dto.getTransactionType())));

        financeRepository.save(Finance.builder()
                .transactionDate(dto.getDate())
                .type(dto.getTransactionType())
                .category(category)
                .detail(dto.getDetail())
                .amount(dto.getAmount())
                .receiptImages(dto.getReceiptImages())
                .build());
    }

    public void updateFinance(Long id, FinanceRequestDto dto) {
        Finance finance = financeRepository.findById(id).orElseThrow();

        FinanceCategory category = categoryRepository.findByNameAndType(dto.getCategory(), dto.getTransactionType())
                .orElseGet(() -> categoryRepository.save(new FinanceCategory(dto.getCategory(), dto.getTransactionType())));

        Schedule schedule = null;
        if (dto.getScheduleId() != null) schedule = scheduleRepository.findById(dto.getScheduleId()).orElse(null);

        finance.update(dto.getDate(), dto.getTransactionType(), category, dto.getDetail(),
                dto.getAmount(), dto.getReceiptImages());
    }

    public void deleteFinance(Long id) {
        financeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(FinanceType type) {
        return categoryRepository.findAllByType(type).stream()
                .map(c -> {
                    CategoryDto dto = new CategoryDto();
                    dto.setId(c.getId());
                    dto.setName(c.getName());
                    dto.setType(c.getType());
                    return dto;
                }).collect(Collectors.toList());
    }

    // 1. 카테고리 수동 생성
    public Long createCategory(String name, FinanceType type) {
        // 중복 체크
        if (categoryRepository.findByNameAndType(name, type).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 카테고리입니다.");
        }
        return categoryRepository.save(new FinanceCategory(name, type)).getId();
    }

    // 2. 카테고리 수정 (이름 변경)
    public void updateCategory(Long id, String newName) {
        FinanceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        // 이름 중복 체크 (본인 이름 제외)
        categoryRepository.findByNameAndType(newName, category.getType())
                .ifPresent(c -> {
                    if (!c.getId().equals(id)) throw new IllegalArgumentException("이미 존재하는 카테고리 이름입니다.");
                });

        category.update(newName);
    }

    // 3. 카테고리 삭제
    public void deleteCategory(Long id) {
        // 1. 삭제할 카테고리 찾기
        FinanceCategory targetCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        // 2. '기타' 카테고리 자체를 삭제하려는 경우 차단 (안전장치)
        if ("기타".equals(targetCategory.getName())) {
            throw new IllegalArgumentException("'기타' 카테고리는 삭제할 수 없습니다.");
        }

        // 3. 이동할 '기타' 카테고리 확보 (Find or Create)
        // 삭제하려는 카테고리와 같은 타입(수입/지출)의 '기타'를 찾음
        FinanceCategory miscCategory = categoryRepository.findByNameAndType("기타", targetCategory.getType())
                .orElseGet(() -> categoryRepository.save(new FinanceCategory("기타", targetCategory.getType())));

        // 4. 기존 기록들 일괄 이동 (Target -> 기타)
        financeRepository.bulkUpdateCategory(targetCategory, miscCategory);

        // 5. 카테고리 삭제
        categoryRepository.delete(targetCategory);
    }
}
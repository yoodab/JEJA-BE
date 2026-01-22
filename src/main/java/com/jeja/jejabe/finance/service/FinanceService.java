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

@Service
@RequiredArgsConstructor
@Transactional
public class FinanceService {

    private final FinanceRepository financeRepository;
    private final FinanceCategoryRepository categoryRepository;
    private final ScheduleRepository scheduleRepository; // 일정 연결용

    // 1. 목록 조회 (잔액 계산 포함)
    @Transactional(readOnly = true)
    public List<FinanceResponseDto> getFinanceList(LocalDate startDate, LocalDate endDate) {
        List<Finance> records = financeRepository.findAllByTransactionDateBetweenOrderByTransactionDateAsc(startDate, endDate);

        long currentBalance = 0L; // (참고: 실제로는 이월금을 가져오는 로직이 필요할 수 있음)

        List<FinanceResponseDto> result = new ArrayList<>();
        for (Finance f : records) {
            if (f.getType() == FinanceType.INCOME) currentBalance += f.getAmount();
            else currentBalance -= f.getAmount();

            result.add(new FinanceResponseDto(f, currentBalance));
        }
        return result;
    }

    // 2. 등록
    public void createFinance(FinanceRequestDto dto) {
        Schedule schedule = null;
        if (dto.getScheduleId() != null) {
            schedule = scheduleRepository.findById(dto.getScheduleId()).orElseThrow();
        }

        // 카테고리 찾기 (없으면 새로 생성 - Find or Create)
        // 또는 "없는 항목입니다" 에러를 내고 관리자가 먼저 등록하게 할 수도 있음. 여기선 자동 생성.
        FinanceCategory category = categoryRepository.findByNameAndType(dto.getCategoryName(), dto.getType())
                .orElseGet(() -> categoryRepository.save(new FinanceCategory(dto.getCategoryName(), dto.getType())));

        financeRepository.save(Finance.builder()
                .transactionDate(dto.getDate())
                .type(dto.getType())
                .category(category)
                .detail(dto.getDetail())
                .amount(dto.getAmount())
                .receiptUrl(dto.getReceiptUrl())
                .schedule(schedule)
                .build());
    }

    // 3. 삭제
    public void deleteFinance(Long id) {
        financeRepository.deleteById(id);
    }

    // 4. 엑셀 업로드
    public int uploadExcel(MultipartFile file) throws IOException {
        List<Finance> finances = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                LocalDate date = row.getCell(0).getLocalDateTimeCellValue().toLocalDate();
                String typeStr = row.getCell(1).getStringCellValue();
                FinanceType type = "수입".equals(typeStr) ? FinanceType.INCOME : FinanceType.EXPENSE;

                String categoryName = row.getCell(2).getStringCellValue();
                // ★ 핵심: 엑셀의 카테고리 텍스트를 DB 엔티티로 변환
                FinanceCategory category = categoryRepository.findByNameAndType(categoryName, type)
                        .orElseGet(() -> categoryRepository.save(new FinanceCategory(categoryName, type)));

                String detail = row.getCell(3) != null ? row.getCell(3).getStringCellValue() : "";
                long amount = 0;
                if (type == FinanceType.INCOME && row.getCell(4) != null) amount = (long) row.getCell(4).getNumericCellValue();
                else if (type == FinanceType.EXPENSE && row.getCell(5) != null) amount = (long) row.getCell(5).getNumericCellValue();

                finances.add(Finance.builder()
                        .transactionDate(date)
                        .type(type)
                        .category(category)
                        .detail(detail)
                        .amount(amount)
                        .build());
            } catch (Exception e) { continue; }
        }
        financeRepository.saveAll(finances);
        return finances.size();
    }

    // 5. 엑셀 다운로드
    public ByteArrayInputStream downloadExcel(LocalDate startDate, LocalDate endDate) throws IOException {
        List<FinanceResponseDto> list = getFinanceList(startDate, endDate);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("회계장부");
            Row header = sheet.createRow(0);
            String[] cols = {"날짜", "구분", "항목", "세부내용", "수입", "지출", "잔액", "관련행사", "영수증"};
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);

            int idx = 1;
            for (FinanceResponseDto dto : list) {
                Row r = sheet.createRow(idx++);
                r.createCell(0).setCellValue(dto.getDate().toString());
                r.createCell(1).setCellValue(dto.getType());
                r.createCell(2).setCellValue(dto.getCategory()); // 문자열로 변환된 이름
                r.createCell(3).setCellValue(dto.getDetail());
                if ("수입".equals(dto.getType())) {
                    r.createCell(4).setCellValue(dto.getIncome());
                    r.createCell(5).setCellValue("");
                } else {
                    r.createCell(4).setCellValue("");
                    r.createCell(5).setCellValue(dto.getExpense());
                }
                r.createCell(6).setCellValue(dto.getBalance());
                r.createCell(7).setCellValue(dto.getRelatedEvent() != null ? dto.getRelatedEvent() : "");
                r.createCell(8).setCellValue(dto.getReceiptUrl() != null ? dto.getReceiptUrl() : "");
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // 카테고리 관리 기능 (목록 조회)
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(FinanceType type) {
        List<FinanceCategory> categories = categoryRepository.findAllByType(type);
        List<CategoryDto> result = new ArrayList<>();
        for(FinanceCategory c : categories) {
            CategoryDto dto = new CategoryDto();
            dto.setId(c.getId());
            dto.setName(c.getName());
            dto.setType(c.getType());
            result.add(dto);
        }
        return result;
    }
}
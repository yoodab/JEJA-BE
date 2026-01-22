package com.jeja.jejabe.member;


import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.domain.MemberRole;
import com.jeja.jejabe.member.domain.MemberStatus;
import com.jeja.jejabe.member.dto.ExcelMemberDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelMemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public int uploadMembersFromExcel(MultipartFile file) {
        if (!isFileValid(file)) {
            throw new GeneralException(CommonErrorCode.BAD_REQUEST); // 잘못된 파일 형식
        }

        List<Member> membersToSave = new ArrayList<>();
        int successCount = 0;

        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0); // 첫 번째 시트

            Iterator<Row> rowIterator = sheet.iterator();

            // 헤더 스킵
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row currentRow = rowIterator.next();
                // 빈 행 처리 (이름이 없는 경우 스킵)
                if (currentRow.getCell(0) == null || currentRow.getCell(0).getStringCellValue().trim().isEmpty()) {
                    continue;
                }

                try {
                    ExcelMemberDto dto = parseExcelRow(currentRow);

                    // DB에 이미 같은 이름/생년월일의 멤버가 있는지 확인
                    if (memberRepository.findByNameAndBirthDate(dto.getName(), dto.getBirthDate()).isPresent()) {
                        log.warn("Skipping duplicate member: Name={}, BirthDate={}", dto.getName(), dto.getBirthDate());
                        continue; // 중복이면 스킵
                    }

                    Member member = Member.builder()
                            .name(dto.getName())
                            .birthDate(dto.getBirthDate())
                            .phone(dto.getPhone())
                            .memberStatus(MemberStatus.ACTIVE)// 엑셀에 없는 경우 기본값
                            .role(MemberRole.MEMBER)
                            .build();
                    membersToSave.add(member);
                    successCount++;

                } catch (IllegalArgumentException e) {
                    log.warn("Skipping row due to data parsing error: {}", e.getMessage());
                }
            }

            memberRepository.saveAll(membersToSave); // 모든 멤버 일괄 저장
        } catch (IOException e) {
            log.error("Error processing Excel file", e);
            throw new GeneralException(CommonErrorCode.INTERNAL_SERVER_ERROR); // 파일 처리 중 오류
        }
        return successCount;
    }

    private boolean isFileValid(MultipartFile file) {
        // 파일이 비어있지 않고, Excel 파일 형식인지 확인
        return file != null && !file.isEmpty() &&
                (file.getContentType().equals("application/vnd.ms-excel") || // .xls
                        file.getContentType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")); // .xlsx
    }

    private ExcelMemberDto parseExcelRow(Row row) {
        ExcelMemberDto dto = new ExcelMemberDto();

        // 이름 (0번째 컬럼)
        Cell nameCell = (Cell) row.getCell(0);
        if (nameCell != null) {
            dto.setName(getCellValueAsString(nameCell));
        } else {
            throw new IllegalArgumentException("이름이 없는 행입니다.");
        }

        // 생년월일 (1번째 컬럼)
        Cell birthDateCell = (Cell) row.getCell(1);
        if (birthDateCell != null) {
            String rawBirthDate = getCellValueAsString(birthDateCell);
            // "98.03.24" -> "1998-03-24" 또는 "99" -> "1999-01-01"과 같이 정규화 필요
            dto.setBirthDate(normalizeBirthDate(rawBirthDate));
        } else {
            dto.setBirthDate(null); // 생년월일은 필수가 아닐 수 있음
        }

        // 연락처 (2번째 컬럼)
        Cell phoneCell = (Cell) row.getCell(2);
        if (phoneCell != null) {
            dto.setPhone(getCellValueAsString(phoneCell).replaceAll("[^0-9]", "")); // 숫자만 추출
        } else {
            dto.setPhone(null);
        }

        return dto;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // 날짜 형식일 수도 있으므로 분기 처리 (날짜는 생년월일 필드에서 따로 처리)
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new DataFormatter().formatCellValue(cell).trim(); // 날짜를 문자열로 포맷
                }
                return String.valueOf((long) cell.getNumericCellValue()).trim(); // 숫자를 문자열로 변환
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue()).trim();
            case FORMULA:
                return cell.getCellFormula().trim();
            default:
                return "";
        }
    }

    // 생년월일 문자열을 YYYY-MM-DD 형식으로 정규화하는 헬퍼 메서드
    private String normalizeBirthDate(String rawBirthDate) {
        if (rawBirthDate == null || rawBirthDate.trim().isEmpty()) {
            return null;
        }
        // 공백 제거 및 불필요한 문자(하이픈, 점 제외) 제거
        String cleanedDate = rawBirthDate.trim().replaceAll("[^0-9.-]", "");

        // 1. "YY.MM.DD" 또는 "YY-MM-DD" 형식 처리
        if (cleanedDate.matches("^\\d{2}[.-]\\d{2}[.-]\\d{2}$")) {
            try {
                int year = Integer.parseInt(cleanedDate.substring(0, 2));
                int month = Integer.parseInt(cleanedDate.substring(3, 5));
                int day = Integer.parseInt(cleanedDate.substring(6, 8));

                // 두 자리 연도를 네 자리로 변환
                int fullYear = (year > LocalDate.now().getYear() % 100) ? 1900 + year : 2000 + year;
                return LocalDate.of(fullYear, month, day).toString(); // "YYYY-MM-DD"
            } catch (Exception e) {
                log.warn("날짜 형식 변환 실패 (YY.MM.DD): '{}'", rawBirthDate);
                return rawBirthDate; // 변환 실패 시 원본 반환
            }
        }

        // 2. "YYYY.MM.DD" 또는 "YYYY-MM-DD" 형식 처리
        if (cleanedDate.matches("^\\d{4}[.-]\\d{2}[.-]\\d{2}$")) {
            return cleanedDate.replace('.', '-');
        }

        // 3. "YYYY.MM" 또는 "YYYY-MM" 형식 처리 (년/월만 있는 경우) - null 처리
        if (cleanedDate.matches("^\\d{4}[.-]\\d{2}$")) {
            log.warn("월까지만 입력된 날짜는 저장하지 않습니다: '{}'", rawBirthDate);
            return null;
        }

        // 4. "YY" 또는 "YYYY" 형식 처리 (년도만 있는 경우) -> null 반환
        if (cleanedDate.matches("^\\d{2}$") || cleanedDate.matches("^\\d{4}$")) {
            log.warn("연도만 입력된 날짜는 저장하지 않습니다: '{}'", rawBirthDate);
            return null;
        }

        // 그 외 처리할 수 없는 형식은 로그를 남기고 null 처리
        log.warn("인식할 수 없는 날짜 형식입니다: '{}'", rawBirthDate);
        return null;
    }
}

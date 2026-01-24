package com.jeja.jejabe.member;


import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.member.domain.Gender;
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
                            .gender(convertGender(dto.getGender()))
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

        Cell genderCell = (Cell) row.getCell(3);
        if (genderCell != null) {
            dto.setGender(getCellValueAsString(genderCell));
        } else {
            dto.setGender(null);
        }

        return dto;
    }

    private Gender convertGender(String genderStr) {
        if (genderStr == null || genderStr.trim().isEmpty()) {
            return null; // 혹은 Gender.NONE (정책에 따라 결정)
        }

        String trimmedGender = genderStr.trim();

        if (trimmedGender.equals("남성") || trimmedGender.equals("남")) {
            return Gender.MALE;
        } else if (trimmedGender.equals("여성") || trimmedGender.equals("여")) {
            return Gender.FEMALE;
        }

        return null; // 알 수 없는 값일 경우 null 처리
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // [핵심 수정] 엑셀이 날짜 포맷인 경우, 바로 LocalDate로 변환하여 "1995-03-15" 형태로 반환
                if (DateUtil.isCellDateFormatted(cell)) {
                    try {
                        return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                    } catch (Exception e) {
                        // 날짜 변환 실패 시 기존 방식대로 문자열 처리 시도
                        return new DataFormatter().formatCellValue(cell).trim();
                    }
                }
                return String.valueOf((long) cell.getNumericCellValue()).trim();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue()).trim();
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (IllegalStateException e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    // 생년월일 문자열을 YYYY-MM-DD 형식으로 정규화하는 헬퍼 메서드
    private LocalDate normalizeBirthDate(String rawBirthDate) {
        if (rawBirthDate == null || rawBirthDate.trim().isEmpty()) {
            return null;
        }

        // [수정 1] 슬래시(/)도 지우지 않고 살려둠
        String cleanedDate = rawBirthDate.trim().replaceAll("[^0-9.\\-/]", "");

        // [수정 2] 점(.)이나 슬래시(/)를 모두 하이픈(-)으로 통일
        String formattedDate = cleanedDate.replace('.', '-').replace('/', '-');

        // [수정 3] "YYYY-M-D" (한 자리수 월/일)도 허용하는 유연한 정규식
        if (formattedDate.matches("^\\d{4}-\\d{1,2}-\\d{1,2}$")) {
            try {
                String[] parts = formattedDate.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);

                // LocalDate를 쓰면 1995-3-5 -> 1995-03-05로 자동 변환됨
                return LocalDate.of(year, month, day);
            } catch (Exception e) {
                log.warn("날짜 변환 실패: {}", rawBirthDate);
                return null;
            }
        }

        // YY-MM-DD 형식 처리 (예: 95-03-15)
        if (formattedDate.matches("^\\d{2}-\\d{1,2}-\\d{1,2}$")) {
            try {
                String[] parts = formattedDate.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);

                // 연도 보정 (2자리 -> 4자리)
                int currentYearShort = LocalDate.now().getYear() % 100;
                int fullYear = (year > currentYearShort) ? 1900 + year : 2000 + year;

                return LocalDate.of(fullYear, month, day);
            } catch (Exception e) {
                return null;
            }
        }

        log.warn("인식할 수 없는 날짜 형식: '{}'", rawBirthDate);
        return null;
    }
}

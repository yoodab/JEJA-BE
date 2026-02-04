package com.jeja.jejabe.newcomer;

import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.member.MemberRepository;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.domain.MemberRole;
import com.jeja.jejabe.member.domain.MemberStatus;
import com.jeja.jejabe.newcomer.domain.Newcomer;
import com.jeja.jejabe.newcomer.domain.NewcomerStatus;
import com.jeja.jejabe.newcomer.dto.*;
import com.jeja.jejabe.auth.UserRepository;
import com.jeja.jejabe.notification.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewcomerService {

    private final NewcomerRepository newcomerRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;

    // 1. 목록 조회 (연도 필터링 + 페이징)
    public Page<NewcomerListResponseDto> getNewcomerList(Integer year, NewcomerStatus status, Pageable pageable) {
        Page<Newcomer> pageResult;

        // 1. 연도(year)가 지정되지 않은 경우 -> 전체 기간 조회
        if (year == null) {
            if (status != null) {
                // 상태 조건은 있고, 날짜 조건은 없음
                pageResult = newcomerRepository.findAllByStatus(status, pageable);
            } else {
                // 상태, 날짜 조건 모두 없음 (완전 전체 조회)
                pageResult = newcomerRepository.findAll(pageable);
            }
        }
        // 2. 연도(year)가 지정된 경우 -> 해당 연도 내에서 조회
        else {
            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year, 12, 31);

            if (status != null) {
                // 상태 + 날짜 조건
                pageResult = newcomerRepository.findAllByStatusAndRegistrationDateBetween(status, startDate, endDate,
                        pageable);
            } else {
                // 날짜 조건만
                pageResult = newcomerRepository.findAllByRegistrationDateBetween(startDate, endDate, pageable);
            }
        }
        return pageResult.map(NewcomerListResponseDto::new);
    }

    // 2. 단건 등록
    @Transactional
    public Long registerNewcomer(NewcomerCreateRequestDto dto) {
        Member manager = null;
        if (dto.getManagerMemberId() != null) {
            manager = findMemberById(dto.getManagerMemberId());
        }

        Newcomer newcomer = Newcomer.builder()
                .name(dto.getName())
                .gender(dto.getGender())
                .birthDate(dto.getBirthDate())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .manager(manager)
                .firstStatus(dto.getFirstStatus())
                .managerName(manager != null ? manager.getName() : null)
                .isChurchRegistered(dto.getIsChurchRegistered() != null && dto.getIsChurchRegistered())
                .build();

        return newcomerRepository.save(newcomer).getNewcomerId();
    }

    // 3. 엑셀 일괄 등록 (Batch)
    @Transactional
    public void registerBatch(List<NewcomerBatchRequestDto> dtoList) {
        for (NewcomerBatchRequestDto dto : dtoList) {
            Member manager = null;
            if (dto.getMdName() != null && !dto.getMdName().isBlank()) {
                manager = memberRepository.findByName(dto.getMdName()).orElse(null);
            }
            boolean isMemberRegistered = Boolean.TRUE.equals(dto.getIsMemberRegistered());

            LocalDate parsedBirthDate = parseDateSafe(dto.getBirthDate());
            LocalDate parsedRegDate = parseDateSafe(dto.getRegistrationDate());

            Newcomer newcomer = Newcomer.builder()
                    .name(dto.getName())
                    .gender(dto.getGender())
                    .birthDate(parsedBirthDate)
                    .phone(dto.getPhone())
                    .address(dto.getAddress())
                    .manager(manager)
                    .managerName(dto.getMdName())
                    .firstStatus(dto.getFirstStatus())
                    .middleStatus(dto.getMiddleStatus())
                    .recentStatus(dto.getRecentStatus())
                    .assignmentNote(dto.getAssignmentNote())
                    .isChurchRegistered(isMemberRegistered)
                    .build();

            if (parsedRegDate != null) {
                newcomer.setRegistrationDateForBatch(parsedRegDate);
            }

            newcomerRepository.save(newcomer);

            // Send notification to manager
            if (manager != null) {
                sendManagerAssignmentNotification(manager, newcomer.getName());
            }
        }
    }

    // 4. 수정
    @Transactional
    public void updateNewcomer(Long newcomerId, NewcomerUpdateRequestDto dto) {
        Newcomer newcomer = findNewcomerById(newcomerId);
        Member newManager = null;
        if (dto.getManagerMemberId() != null) {
            newManager = findMemberById(dto.getManagerMemberId());
        }

        Member oldManager = newcomer.getManager();

        newcomer.updateInfo(
                dto.getAddress(), dto.getPhone(), dto.getAssignmentNote(),
                dto.getFirstStatus(), dto.getMiddleStatus(), dto.getRecentStatus(),
                dto.getProfileImageUrl(), newManager, dto.getBirthDate(),
                dto.getIsChurchRegistered(), dto.getGender(), dto.getCellName());

        // Send notification if manager changed or assigned
        if (newManager != null && (oldManager == null || !oldManager.getId().equals(newManager.getId()))) {
            sendManagerAssignmentNotification(newManager, newcomer.getName());
        }
    }

    private void sendManagerAssignmentNotification(Member manager, String newcomerName) {
        try {
            userRepository.findByMember(manager).ifPresent(user -> fcmService.sendNotificationToUser(user.getId(),
                    "새신자 담당 알림", newcomerName + "님이 담당 새신자로 배정되었습니다."));
        } catch (Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }

    // 5. 등반 / 상태변경 / 상세조회 Helper
    @Transactional
    public Long registerNewcomerAsMember(Long newcomerId) {
        Newcomer newcomer = findNewcomerById(newcomerId);

        Member newMember = Member.builder()
                .name(newcomer.getName())
                .gender(newcomer.getGender())
                .birthDate(newcomer.getBirthDate())
                .phone(newcomer.getPhone())
                .memberStatus(MemberStatus.NEWCOMER)
                .memberImageUrl(newcomer.getProfileImageUrl())
                .role(MemberRole.MEMBER)
                .build();
        Member savedMember = memberRepository.save(newMember);

        newcomer.registerAsMember(savedMember);
        return savedMember.getId();
    }

    @Transactional
    public void changeNewcomerStatus(Long newcomerId, NewcomerStatus newStatus) {
        Newcomer newcomer = findNewcomerById(newcomerId);
        newcomer.changeStatus(newStatus);
    }

    @Transactional
    public void deleteNewcomer(Long newcomerId) {
        Newcomer newcomer = findNewcomerById(newcomerId);
        newcomerRepository.delete(newcomer);
    }

    public NewcomerDetailResponseDto getNewcomerDetails(Long newcomerId) {
        return new NewcomerDetailResponseDto(findNewcomerById(newcomerId));
    }

    private Newcomer findNewcomerById(Long id) {
        return newcomerRepository.findById(id)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.NEWCOMER_NOT_FOUND));
    }

    private Member findMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.MEMBER_NOT_FOUND));
    }

    private LocalDate parseDateSafe(String dateStr) {
        if (dateStr == null || dateStr.isBlank())
            return null;
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
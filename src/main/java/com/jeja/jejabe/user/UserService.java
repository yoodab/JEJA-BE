package com.jeja.jejabe.user;

import com.jeja.jejabe.attendance.AttendanceRepository;
import com.jeja.jejabe.auth.User;
import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.auth.UserRepository;
import com.jeja.jejabe.auth.UserStatus;
import com.jeja.jejabe.care.domain.CareStatus;
import com.jeja.jejabe.care.repository.AbsenceCareRepository;
import com.jeja.jejabe.cell.Cell;
import com.jeja.jejabe.cell.MemberCellHistory;
import com.jeja.jejabe.cell.MemberCellHistoryRepository;
import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.member.MemberRepository;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.domain.MemberRole;
import com.jeja.jejabe.member.domain.MemberStatus;
import com.jeja.jejabe.newcomer.NewcomerRepository;
import com.jeja.jejabe.user.dto.AdminDashboardStatsDto;
import com.jeja.jejabe.user.dto.MyInfoResponseDto;
import com.jeja.jejabe.user.dto.MyInfoUpdateRequestDto;
import com.jeja.jejabe.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final MemberCellHistoryRepository memberCellHistoryRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AttendanceRepository attendanceRepository;
    private final NewcomerRepository newcomerRepository;
    private final AbsenceCareRepository absenceCareRepository;

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow();
        String encodePassword = passwordEncoder.encode(newPassword);
        user.changePassword(encodePassword);
    }

    public AdminDashboardStatsDto getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 이번 주 주일예배 출석 수 (전체 카운트로 대체)
        long attendanceCount = attendanceRepository.count();

        // 2. 이번 달 새신자 수
        LocalDateTime startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();
        long newcomerCount = newcomerRepository.countByRegistrationDateBetween(startOfMonth.toLocalDate(),
                now.toLocalDate());

        // 3. 장기 결석자 수
        long absenteeCount = absenceCareRepository.countByStatus(CareStatus.LONG_TERM_ABSENCE);

        // 4. 승인 대기 유저 수
        long pendingUserCount = userRepository.countByStatus(UserStatus.PENDING);

        return AdminDashboardStatsDto.builder()
                .weeklyAttendanceCount(attendanceCount)
                .monthlyNewcomerCount(newcomerCount)
                .longTermAbsenteeCount(absenteeCount)
                .pendingUserCount(pendingUserCount)
                .build();
    }

    public MyInfoResponseDto getMyInfo(User user) {
        Member member = user.getMember();

        // [수정] 관리자 계정 (Member가 없는 경우) 처리
        if (member == null) {
            return MyInfoResponseDto.fromAdmin(user);
        }

        // 1. 활동 중인 모든 순 기록 조회
        List<MemberCellHistory> activeHistories = memberCellHistoryRepository.findAllByMemberAndIsActiveTrue(member);

        // 2. 가장 최근에 시작한 순 하나 선택
        Cell primaryCell = activeHistories.stream()
                .sorted(Comparator.comparing(MemberCellHistory::getStartDate).reversed())
                .map(MemberCellHistory::getCell)
                .findFirst()
                .orElse(null);

        return MyInfoResponseDto.fromMember(user, member, primaryCell);
    }

    /**
     * 사용자 목록 조회 (상태 필터링 지원)
     * status가 null이면 전체 조회, 값이 있으면 해당 상태만 조회
     */
    @Transactional(readOnly = true)
    public List<UserResponseDto> getUsers(UserStatus status, UserDetailsImpl userDetails) {

        List<User> users;

        if (status == null) {
            users = userRepository.findAll(); // 전체 조회
        } else {
            users = userRepository.findAllByStatus(status); // 상태별 조회
        }

        return users.stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 상태 변경 (승인, 거절, 정지 등 모든 상태 변경 가능)
     */
    @Transactional
    public void updateUserStatus(Long userId, UserStatus newStatus, UserDetailsImpl userDetails) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.USER_NOT_FOUND));

        // 1. 유저 상태 변경
        user.updateStatus(newStatus);

        // 2. 만약 상태가 '활성(ACTIVE)'으로 변경되었고, 연결된 Member가 있다면
        if (newStatus == UserStatus.ACTIVE && user.getMember().getMemberStatus() == MemberStatus.INACTIVE) {
            Member member = user.getMember();

            // 멤버의 상태를 ACTIVE로, 직분을 SAEGAJOK(새가족) 또는 일반 성도로 변경
            member.markAsNewcomer();
            member.addRole(MemberRole.MEMBER);
        }
    }

    @Transactional
    public void updateMyInfo(Long userId, MyInfoUpdateRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.USER_NOT_FOUND));

        Member member = user.getMember();

        // 1. Member 정보 수정 (연락처, 프로필 사진)
        // 관리자가 아닌 일반 유저가 본인 정보를 수정할 때 허용되는 필드만 업데이트
        if (member != null) {
            String newPhone = requestDto.getPhone();

            // 전화번호 검증 및 포맷팅
            if (newPhone != null && !newPhone.isBlank()) {
                // 숫자만 추출
                String rawPhone = newPhone.replaceAll("[^0-9]", "");

                // 010으로 시작하고 11자리인지 확인
                if (!rawPhone.startsWith("010") || rawPhone.length() != 11) {
                    throw new GeneralException(CommonErrorCode.INVALID_PHONE_FORMAT);
                }

                // 010-XXXX-XXXX 형식으로 변환
                String formattedPhone = rawPhone.substring(0, 3) + "-" +
                        rawPhone.substring(3, 7) + "-" +
                        rawPhone.substring(7);

                // 중복 검사 (내 번호 제외)
                // if (memberRepository.existsByPhoneAndIdNot(formattedPhone, member.getId())) {
                // throw new GeneralException(CommonErrorCode.DUPLICATE_PHONE);
                // }

                // 검증된 번호로 업데이트
                newPhone = formattedPhone;
            }

            member.updatePhone(newPhone);
            user.updatePhone(newPhone);
        }

        // 2. User 정보 수정 (이메일)
        if (requestDto.getEmail() != null) {
            user.updateEmail(requestDto.getEmail());
        }

        if (requestDto.getProfileImageUrl() != null) {
            user.updateProfileImage(requestDto.getProfileImageUrl());
        }

        // 3. 비밀번호 변경 로직
        if (requestDto.getNewPassword() != null && !requestDto.getNewPassword().isBlank()) {
            // 현재 비밀번호 확인
            if (requestDto.getCurrentPassword() == null ||
                    !passwordEncoder.matches(requestDto.getCurrentPassword(), user.getPassword())) {
                throw new GeneralException(CommonErrorCode.INVALID_PASSWORD);
            }
            user.updatePassword(passwordEncoder.encode(requestDto.getNewPassword()));
        }
    }

    @Transactional
    public void withdraw(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.USER_NOT_FOUND));

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new GeneralException(CommonErrorCode.INVALID_PASSWORD);
        }

        // 유저 삭제 (Member는 유지)
        userRepository.delete(user);
    }

}
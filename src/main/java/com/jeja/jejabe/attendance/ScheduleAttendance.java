package com.jeja.jejabe.attendance;

import com.jeja.jejabe.global.entity.BaseTimeEntity;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.schedule.domain.Schedule;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "schedule_attendance",
        uniqueConstraints = {
                // [수정] 일정 ID + 멤버 ID + "해당 일정의 기준 날짜"가 유니크해야 함
                @UniqueConstraint(columnNames = {"schedule_id", "member_id", "schedule_date"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleAttendance extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attendanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    private LocalDateTime attendanceTime;

    @Enumerated(EnumType.STRING)
    private AttendanceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    private Double latitude;
    private Double longitude;

    @Column(length = 45)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    private AttendanceSource source;

    public void updateSource(AttendanceSource source) {
        this.source = source;
    }

    public void markAsPresent(LocalDateTime time, AttendanceType type, Double lat, Double lon, String ip, AttendanceSource source) {
        this.attendanceTime = time;
        this.type = type;
        this.latitude = lat;
        this.longitude = lon;
        this.ipAddress = ip;
        this.source = source;
        this.status = AttendanceStatus.PRESENT;
    }

    @Builder
    public ScheduleAttendance(Schedule schedule, Member member, LocalDate scheduleDate, LocalDateTime attendanceTime, AttendanceType type, Double latitude, Double longitude, String ipAddress, AttendanceSource source, AttendanceStatus status) {
        this.schedule = schedule;
        this.member = member;
        this.scheduleDate = scheduleDate;
        this.attendanceTime = attendanceTime;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.ipAddress = ipAddress;
        this.source = source;
        this.status = status != null ? status : (attendanceTime != null ? AttendanceStatus.PRESENT : AttendanceStatus.REGISTERED);
    }

    public void updateStatus(AttendanceStatus status, AttendanceType type, AttendanceSource source) {
        this.status = status;
        // 출석 처리일 경우에만 메타데이터 업데이트
        if (status == AttendanceStatus.PRESENT) {
            if (this.attendanceTime == null) {
                this.attendanceTime = LocalDateTime.now();
            }
            // 기존 타입/소스가 없을 때만 덮어쓰거나, 강제로 덮어쓰거나 정책 결정
            // 여기서는 관리자가 체크하면 ADMIN으로 변경하도록 함
            if (type != null) this.type = type;
            if (source != null) this.source = source;
        }
        // 출석 취소(REGISTERED)일 경우 시간 초기화 여부는 선택 (보통은 기록 남김)
    }
}

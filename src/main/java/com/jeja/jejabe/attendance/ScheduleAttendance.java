package com.jeja.jejabe.attendance;

import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.schedule.domain.Schedule;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "schedule_attendance",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"schedule_id", "member_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attendanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDateTime attendanceTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceType type;

    private Double latitude;
    private Double longitude;

    @Column(length = 45)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    private AttendanceSource source;

    public void updateSource(AttendanceSource source) {
        this.source = source;
    }

    @Builder
    public ScheduleAttendance(Schedule schedule, Member member, LocalDateTime attendanceTime, AttendanceType type, Double latitude, Double longitude, String ipAddress, AttendanceSource source) {
        this.schedule = schedule;
        this.member = member;
        this.attendanceTime = attendanceTime;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.ipAddress = ipAddress;
        this.source = source;
    }
}

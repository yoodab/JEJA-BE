package com.jeja.jejabe.auth;

import com.jeja.jejabe.global.entity.BaseTimeEntity;
import com.jeja.jejabe.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users") // 'user'는 DB 예약어일 수 있으므로 'users'를 권장
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 protected 기본 생성자
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 50) // null 불가, 고유값
    private String loginId;

    @Column(nullable = false) // 비밀번호는 길이를 넉넉하게
    private String password;

    @Column(nullable = false, length = 20)
    private UserRole userRole; // 권한 (예: "ROLE_ADMIN", "ROLE_LEADER", "ROLE_USER")

    @Enumerated(EnumType.STRING) // Enum 이름을 DB에 문자열로 저장
    @Column(nullable = false)
    private UserStatus status; // 계정 상태 (PENDING, ACTIVE, INACTIVE)

    // ========================================================================
    // ★★★ Member와의 관계 설정 (1:1) ★★★
    // ========================================================================
    // FetchType.LAZY: User 정보 조회 시 Member 정보는 즉시 로딩하지 않음.
    // optional = false: User는 반드시 Member와 연결되어야 함. (우리 정책)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false) // 외래 키(FK). users 테이블에 member_id 컬럼 생성
    private Member member;

    @Column(unique = true)
    private String email; // 이메일 필드 추가

    @Column(length = 500)
    private String profileImageUrl; // 유저 프로필 이미지 URL

    private String phone;

    // 생성자나 빌더에도 email 추가 필요
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updatePhone(String phone) {
        this.phone = phone;
    }

    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    // ========================================================================
    // 생성자 (Builder 패턴 사용)
    // ========================================================================
    @Builder // Builder 패턴을 위한 어노테이션
    public User(String loginId, String password, UserRole userRole, UserStatus status, Member member, String email,
            String profileImageUrl,String phone) {
        this.loginId = loginId;
        this.password = password;
        this.userRole = userRole;
        this.status = status;
        this.member = member;
        this.email = email;
        this.phone = phone;
        this.profileImageUrl = profileImageUrl;
    }

    // ========================================================================
    // 비즈니스 로직 (상태 변경 메소드 등)
    // ========================================================================

    // 관리자가 계정을 승인할 때 호출
    public void approve() {
        if (this.status != UserStatus.PENDING) {
            throw new IllegalStateException("승인 대기 상태의 사용자만 승인할 수 있습니다.");
        }
        this.status = UserStatus.ACTIVE;
    }

    // 관리자가 계정을 비활성화할 때 호출
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    // 사용자가 비밀번호를 변경할 때 호출
    // (PasswordEncoder를 서비스 계층에서 받아와서 처리해야 함)
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateStatus(UserStatus status) {
        this.status = status;
    }

    // 양방향 연관관계 편의 메소드 (Member.java의 setUser와 함께 사용)
    // 빌더에서 member를 설정하면, 이 메소드를 명시적으로 호출할 필요는 없을 수 있습니다.
    public void setMember(Member member) {
        this.member = member;
        if (member.getUser() != this) {
            member.setUser(this);
        }
    }
}
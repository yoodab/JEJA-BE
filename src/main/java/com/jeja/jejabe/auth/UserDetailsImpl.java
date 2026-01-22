package com.jeja.jejabe.auth;

import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.domain.MemberRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

public class UserDetailsImpl implements UserDetails {

    private final User user;

    public UserDetailsImpl(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getLoginId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> authorities = new ArrayList<>();

        // 1. [시스템 관리자] (최상위)
        // ADMIN 권한이 있으면 RoleHierarchy에 의해 아래 모든 권한을 가진 것으로 처리됨
        if (user.getUserRole() == UserRole.ROLE_ADMIN) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            return authorities;
        }

        if (user.getUserRole() == UserRole.ROLE_PASTOR) {
            authorities.add(new SimpleGrantedAuthority("ROLE_PASTOR"));
            return authorities;
        }


        // 2. [멤버의 여러 직분 매핑]
        Member member = user.getMember();
        if (member != null) {
            // 멤버가 가진 역할 Set을 순회하며 모두 추가
            // 예: 이 사람이 SOONJANG, TEAM_LEADER를 둘 다 가지고 있다면
            // authorities에는 ["ROLE_SOONJANG", "ROLE_TEAM_LEADER"] 두 개가 들어감.
            for (MemberRole role : member.getRoles()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
            }
        }

        // 3. [기본 유저 권한]
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 계정이 활성화(ACTIVE) 상태일 때만 로그인 가능
        return user.getStatus() == UserStatus.ACTIVE;
    }
}

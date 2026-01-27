package com.jeja.jejabe.config;

import com.jeja.jejabe.auth.User;
import com.jeja.jejabe.auth.UserRepository;
import com.jeja.jejabe.auth.UserRole;
import com.jeja.jejabe.auth.UserStatus;
import com.jeja.jejabe.board.domain.Board;
import com.jeja.jejabe.board.domain.BoardAccessType;
import com.jeja.jejabe.board.repository.BoardRepository;
import com.jeja.jejabe.club.Club;
import com.jeja.jejabe.club.ClubRepository;
import com.jeja.jejabe.club.ClubType;
import com.jeja.jejabe.member.MemberRepository;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.domain.MemberStatus;
import com.jeja.jejabe.schedule.domain.WorshipCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final BoardRepository boardRepository;
    private final ClubRepository clubRepository;


    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. 슈퍼 관리자 및 멤버 생성
        Member adminMember = createAdminIfNeeded();

        // 2. 필수 게시판 생성 (프론트엔드 boardKey 매칭용)
        createBoardIfNeeded("공지사항", "notice", "청년부 주요 소식을 전합니다.", BoardAccessType.PUBLIC);
        createBoardIfNeeded("자유게시판", "free", "자유롭게 소통하는 공간입니다.", BoardAccessType.MEMBER);
        createBoardIfNeeded("기도제목", "prayer", "함께 기도를 나누는 공간입니다.", BoardAccessType.MEMBER);
        createBoardIfNeeded("목사님께 질문", "question", "무엇이든 물어보세요.", BoardAccessType.MEMBER);

        // 3. 필수 시스템 팀 생성 (새신자 관리 등을 위해 필요)
        createClubIfNeeded("새신자팀", "새신자를 환영하고 정착을 돕는 팀입니다.", ClubType.NEW_BELIEVER, null);
        createClubIfNeeded("예배팀", "예배 순서를 기획하고 준비하는 팀입니다.", ClubType.WORSHIP, null);
        createClubIfNeeded("방송팀", "음향 및 영상 송출을 담당합니다.", ClubType.BROADCAST, null);

    }

    private Member createAdminIfNeeded() {
        if (!userRepository.existsByLoginId("admin")) {
            Member adminMember = Member.builder()
                    .name("관리자")
                    .memberStatus(MemberStatus.SYSTEM)
                    .build();
            Member savedMember = memberRepository.save(adminMember);

            User admin = User.builder()
                    .loginId("admin")
                    .password(passwordEncoder.encode("admin1234"))
                    .userRole(UserRole.ROLE_ADMIN)
                    .status(UserStatus.ACTIVE)
                    .member(savedMember)
                    .build();
            userRepository.save(admin);
            System.out.println(">>> [INIT] 슈퍼 관리자 계정이 생성되었습니다. (ID: admin / PW: admin1234)");
            return savedMember;
        }
        return userRepository.findByLoginId("admin").get().getMember();
    }

    private void createBoardIfNeeded(String name, String key, String desc, BoardAccessType type) {
        if (boardRepository.findByBoardKey(key).isEmpty()) {
            Board board = Board.builder()
                    .name(name)
                    .boardKey(key)
                    .description(desc)
                    .accessType(type)
                    .build();
            boardRepository.save(board);
            System.out.println(">>> [INIT] 게시판 생성 완료: " + name);
        }
    }

    private void createClubIfNeeded(String name, String desc, ClubType type, Member leader) {
        // 이름으로 팀 존재 여부 확인 (또는 별도 key 컬럼이 있다면 그것으로 확인)
        if (clubRepository.findAll().stream().noneMatch(c -> c.getName().equals(name))) {
            Club club = Club.builder()
                    .name(name)
                    .description(desc)
                    .type(type)
                    .leader(leader)
                    .meetingPlace("청년부실")
                    .build();
            clubRepository.save(club);
            System.out.println(">>> [INIT] 시스템 팀 생성 완료: " + name);
        }
    }


}

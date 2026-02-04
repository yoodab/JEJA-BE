package com.jeja.jejabe.auth;

import com.jeja.jejabe.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByStatus(UserStatus status);

    boolean existsByLoginId(String admin);

    @Query("SELECT u FROM User u JOIN FETCH u.member m LEFT JOIN FETCH m.roles WHERE u.loginId = :loginId")
    Optional<User> findByLoginId(@Param("loginId") String loginId);

    long countByStatus(UserStatus status);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByMember(com.jeja.jejabe.member.domain.Member member);

    boolean existsByMember(Member member);

    @Query("SELECT u FROM User u WHERE u.member.id IN :memberIds")
    List<User> findAllByMemberIds(@Param("memberIds") List<Long> memberIds);

    @Query("SELECT u FROM User u JOIN u.member m JOIN m.roles r WHERE r = com.jeja.jejabe.member.domain.MemberRole.CELL_LEADER")
    List<User> findAllSoonjangs();
}

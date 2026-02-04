package com.jeja.jejabe.album;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    // 사용자 권한에 따라 볼 수 있는 앨범만 조회하는 쿼리
    // 1. PUBLIC은 누구나
    // 2. MEMBERS_ONLY는 멤버(로그인한 사람) 이상
    // 3. ADMIN_ONLY는 관리자만
    // 4. 관리자라면(:isAdmin = true) 무조건 다 보임
    @Query("SELECT a FROM Album a WHERE " +
            "(:isAdmin = true) OR " +
            "(a.readPermission = 'PUBLIC_READ') OR " +
            "(a.readPermission = 'MEMBERS_ONLY_READ' AND :isMember = true)"+
            "ORDER BY a.createdAt DESC")
    Page<Album> findAllReadable(@Param("isMember") boolean isMember, @Param("isAdmin") boolean isAdmin, Pageable pageable);
}

package com.jeja.jejabe.album;

import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("albumGuard")
@RequiredArgsConstructor
public class AlbumGuard {

    private final AlbumRepository albumRepository;

    /**
     * 앨범 읽기 권한 체크
     */
    @Transactional(readOnly = true)
    public boolean canRead(Long albumId, UserDetailsImpl userDetails) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.ALBUM_NOT_FOUND));

        PermissionType permission = album.getReadPermission();

        // 1. 전체 공개라면 로그인 안 해도 됨 (userDetails가 null일 수 있음)
        if (permission == PermissionType.PUBLIC_READ) return true;

        // 2. 여기서부터는 로그인 필수
        if (userDetails == null) return false;

        // 3. 멤버 공개 (로그인만 했으면 OK)
        if (permission == PermissionType.MEMBERS_ONLY_READ) return true;

        // 4. 관리자 공개 (ROLE_ADMIN 권한 확인)
        // Spring Security가 authorities를 관리하므로, 여기서 hasRole 체크도 가능하지만
        // 간단하게 UserRole로 체크하거나, userDetails.getAuthorities()를 확인해도 됨.
        if (permission == PermissionType.ADMIN_ONLY_READ) {
            return isAdmin(userDetails);
        }

        return false;
    }

    /**
     * 앨범 쓰기/수정 권한 체크
     */
    @Transactional(readOnly = true)
    public boolean canWrite(Long albumId, UserDetailsImpl userDetails) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.ALBUM_NOT_FOUND));

        PermissionType permission = album.getWritePermission();

        if (userDetails == null) return false;

        // 관리자 전용 쓰기
        if (permission == PermissionType.ADMIN_WRITE) {
            return isAdmin(userDetails);
        }

        if (permission == PermissionType.MEMBERS_WRITE) return true;

        // (만약 추후에 MEMBERS_WRITE가 생긴다면 여기서 처리)

        return false;
    }

    private boolean isAdmin(UserDetailsImpl userDetails) {
        if (userDetails == null) return false;

        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_PASTOR")
                        || a.getAuthority().equals("ROLE_EXECUTIVE"));
    }
}

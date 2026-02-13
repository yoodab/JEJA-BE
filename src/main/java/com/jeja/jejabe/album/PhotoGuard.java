package com.jeja.jejabe.album;

import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.auth.UserRole;
import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.domain.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component("photoGuard")
@RequiredArgsConstructor
public class PhotoGuard {

    private final PhotoRepository photoRepository;

    @Transactional(readOnly = true)
    public boolean canDelete(Long photoId, UserDetailsImpl userDetails) {
        if (userDetails == null) return false;

        // 1. 운영진(관리자, 임원, 목사님) 프리패스 체크
        if (hasAdminPrivilege(userDetails)) {
            return true;
        }

        // 2. 작성자 본인 확인
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.PHOTO_NOT_FOUND));

        Member uploader = photo.getUploader();
        Member currentMember = userDetails.getUser().getMember();

        // 업로더 정보가 없거나, 현재 로그인 멤버가 없으면 삭제 불가
        if (uploader == null || currentMember == null) return false;

        return uploader.getId().equals(currentMember.getId());
    }

    // 운영진 권한 체크 헬퍼
    private boolean hasAdminPrivilege(UserDetailsImpl userDetails) {
        return userDetails != null && userDetails.getUser().isPrivileged();
    }
}

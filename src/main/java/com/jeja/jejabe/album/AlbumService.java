package com.jeja.jejabe.album;

import com.jeja.jejabe.album.dto.AlbumCreateRequestDto;
import com.jeja.jejabe.album.dto.AlbumResponseDto;
import com.jeja.jejabe.album.dto.AlbumUpdateRequestDto;
import com.jeja.jejabe.album.dto.PhotoResponseDto;
import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.auth.UserRole;
import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.domain.MemberRole;
import com.jeja.jejabe.schedule.domain.Schedule;
import com.jeja.jejabe.schedule.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final PhotoRepository photoRepository;
    private final ScheduleRepository scheduleRepository;

    // 앨범 생성
    public Long createAlbum(AlbumCreateRequestDto dto) {
        Schedule schedule = null;
        if (dto.getScheduleId() != null) {
            schedule = scheduleRepository.findById(dto.getScheduleId()).orElse(null);
        }

        Album album = Album.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .schedule(schedule)
                .readPermission(dto.getReadPermission())
                .writePermission(dto.getWritePermission())
                .build();
        return albumRepository.save(album).getAlbumId();
    }

    // 전체 앨범 목록 조회 (사용자 권한에 맞는 앨범만 DB에서 조회)
    @Transactional(readOnly = true)
    public Page<AlbumResponseDto> getAllAlbums(UserDetailsImpl userDetails, Pageable pageable) {
        boolean isMember = (userDetails != null);
        boolean isAdmin = hasAdminPrivilege(userDetails);

        return albumRepository.findAllReadable(isMember, isAdmin, pageable)
                .map(AlbumResponseDto::new);
    }

    // 앨범 단건 조회 (사진 목록 포함)
    @Transactional(readOnly = true)
    public List<PhotoResponseDto> getPhotosByAlbum(Long albumId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.ALBUM_NOT_FOUND));

        // Guard(@PreAuthorize)를 통과했다면 읽기 권한은 이미 검증된 상태임
        return album.getPhotos().stream()
                .map(PhotoResponseDto::new)
                .collect(Collectors.toList());
    }

    // 사진 업로드
    public void addPhotos(Long albumId, List<String> imageUrls, UserDetailsImpl userDetails) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.ALBUM_NOT_FOUND));

        Member uploader = userDetails.getUser().getMember();
        List<Photo> newPhotos = imageUrls.stream()
                .map(url -> Photo.builder()
                        .album(album)
                        .imageUrl(url)
                        .uploader(uploader)
                        .build())
                .collect(Collectors.toList());

        photoRepository.saveAll(newPhotos);
    }

    // 앨범 정보 수정
    public void updateAlbum(Long albumId, AlbumUpdateRequestDto dto) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.ALBUM_NOT_FOUND));

        album.update(dto.getTitle(), dto.getDescription(), dto.getReadPermission(), dto.getWritePermission());
    }

    // 앨범 삭제
    public void deleteAlbum(Long albumId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.ALBUM_NOT_FOUND));

        albumRepository.delete(album);
    }

    // 사진 삭제
    public void deletePhoto(Long photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.PHOTO_NOT_FOUND));

        // 주의: 사진 삭제 권한은 앨범 쓰기 권한과 동일하다고 가정 (Guard에서 앨범ID로 체크 필요)
        // 만약 컨트롤러에서 photoId만 받아서 체크하기 어렵다면 여기서 앨범 권한을 다시 체크하거나,
        // PhotoGuard를 만들어야 함. 지금은 컨트롤러가 앨범 권한을 체크했다고 가정.
        photoRepository.delete(photo);
    }

    public void updatePhoto(Long photoId, String caption) {
        Photo photo = photoRepository.findById(photoId).orElseThrow();
        photo.update(caption); // 엔티티 메소드 필요
    }


    private boolean hasAdminPrivilege(UserDetailsImpl userDetails) {
        if (userDetails == null) return false;

        // 1. 시스템 관리자(UserRole) 체크
        if (userDetails.getUser().getUserRole() == UserRole.ROLE_ADMIN || userDetails.getUser().getUserRole() == UserRole.ROLE_PASTOR) {
            return true;
        }

        // 2. 교회 직분(MemberRole) 체크 - 임원 또는 교역자
        Member member = userDetails.getUser().getMember();
        if (member != null) {
            Set<MemberRole> roles = member.getRoles();
            // roles 컬렉션 안에 해당 직분이 포함되어 있는지 확인
            return roles.contains(MemberRole.EXECUTIVE);
        }

        return false;
    }
}

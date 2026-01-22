package com.jeja.jejabe.album;

import com.jeja.jejabe.album.dto.AlbumCreateRequestDto;
import com.jeja.jejabe.album.dto.AlbumResponseDto;
import com.jeja.jejabe.album.dto.AlbumUpdateRequestDto;
import com.jeja.jejabe.album.dto.PhotoResponseDto;
import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.global.response.ApiResponseForm;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    // (관리자) 앨범 생성
    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR','EXECUTIVE')")
    @PostMapping("/admin/albums")
    public ResponseEntity<ApiResponseForm<Long>> createAlbum(
            @RequestBody AlbumCreateRequestDto dto
    ) {
        Long albumId = albumService.createAlbum(dto);
        return ResponseEntity.ok(ApiResponseForm.success(albumId, "앨범이 생성되었습니다."));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR','EXECUTIVE')")
    @PatchMapping("/admin/albums/{albumId}")
    public ResponseEntity<ApiResponseForm<Void>> updateAlbum(
            @PathVariable Long albumId,
            @RequestBody AlbumUpdateRequestDto dto) {
        albumService.updateAlbum(albumId, dto);
        return ResponseEntity.ok(ApiResponseForm.success("앨범 정보가 수정되었습니다."));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR','EXECUTIVE')")
    @DeleteMapping("/admin/albums/{albumId}")
    public ResponseEntity<ApiResponseForm<Void>> deleteAlbum(
            @PathVariable Long albumId) {
        albumService.deleteAlbum(albumId);
        return ResponseEntity.ok(ApiResponseForm.success("앨범이 삭제되었습니다."));
    }

    @GetMapping("/albums")
    public ResponseEntity<ApiResponseForm<Page<AlbumResponseDto>>> getAllAlbums(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AlbumResponseDto> albums = albumService.getAllAlbums(userDetails, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(albums));
    }

    @GetMapping("/albums/{albumId}/photos")
    public ResponseEntity<ApiResponseForm<List<PhotoResponseDto>>> getPhotosByAlbum(
            @PathVariable Long albumId) {
        List<PhotoResponseDto> photos = albumService.getPhotosByAlbum(albumId);
        return ResponseEntity.ok(ApiResponseForm.success(photos, "사진 목록 조회 성공"));
    }

    @PreAuthorize("@albumGuard.canWrite(#albumId, principal)")
    @PostMapping("/albums/{albumId}/photos")
    public ResponseEntity<ApiResponseForm<Void>> uploadPhotos(
            @PathVariable Long albumId,
            @RequestBody List<String> imageUrls,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        albumService.addPhotos(albumId, imageUrls, userDetails);
        return ResponseEntity.ok(ApiResponseForm.success("사진이 업로드되었습니다."));
    }

    @PatchMapping("/albums/{albumId}/photos/{photoId}")
    public ResponseEntity<ApiResponseForm<Void>> updatePhoto(
            @PathVariable Long albumId,
            @PathVariable Long photoId,
            @RequestBody PhotoResponseDto dto // caption만 받음
    ) {
        albumService.updatePhoto(photoId, dto.getCaption());
        return ResponseEntity.ok(ApiResponseForm.success("사진 정보가 수정되었습니다."));
    }

    @PreAuthorize("@photoGuard.canDelete(#photoId, principal)")
    @DeleteMapping("/photos/{photoId}")
    public ResponseEntity<ApiResponseForm<Void>> deletePhoto(
            @PathVariable Long photoId) {
        albumService.deletePhoto(photoId);
        return ResponseEntity.ok(ApiResponseForm.success("사진이 삭제되었습니다."));
    }
}

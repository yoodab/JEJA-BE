package com.jeja.jejabe.album.dto;

import com.jeja.jejabe.album.Album;
import lombok.Getter;

@Getter
public class AlbumResponseDto {
    private final Long albumId;
    private final String title;
    private final String description;
    private final String coverImageUrl;

    public AlbumResponseDto(Album album) {
        this.albumId = album.getAlbumId();
        this.title = album.getTitle();
        this.description = album.getDescription();
        this.coverImageUrl = album.getPhotos().isEmpty() ? null : album.getPhotos().get(0).getImageUrl();
    }
}

package com.jeja.jejabe.album.dto;

import com.jeja.jejabe.album.Photo;
import lombok.Getter;

@Getter
public class PhotoResponseDto {
    private final Long photoId;
    private final String imageUrl;
    private final String caption;

    public PhotoResponseDto(Photo photo) {
        this.photoId = photo.getPhotoId();
        this.imageUrl = photo.getImageUrl();
        this.caption = photo.getCaption();
    }
}

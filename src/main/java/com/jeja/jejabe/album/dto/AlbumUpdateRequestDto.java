package com.jeja.jejabe.album.dto;

import com.jeja.jejabe.album.PermissionType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlbumUpdateRequestDto {
    private String title;
    private String description;
    private PermissionType readPermission;
    private PermissionType writePermission;
}

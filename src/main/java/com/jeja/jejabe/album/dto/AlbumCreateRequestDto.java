package com.jeja.jejabe.album.dto;

import com.jeja.jejabe.album.PermissionType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlbumCreateRequestDto {
    private String title;
    private String description;
    private Long scheduleId;
    private PermissionType readPermission;
    private PermissionType writePermission;
}

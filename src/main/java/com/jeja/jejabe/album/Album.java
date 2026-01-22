package com.jeja.jejabe.album;

import com.jeja.jejabe.global.entity.BaseTimeEntity;
import com.jeja.jejabe.schedule.domain.Schedule;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Album extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long albumId;

    @Column(nullable = false, length = 200)
    private String title;

    private String description;

    // ★★★ 일정(Event)과의 선택적 1:1 연결 ★★★
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", unique = true)
    private Schedule schedule;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Photo> photos = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionType readPermission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionType writePermission;

    @Builder
    public Album(String title, String description, Schedule schedule, PermissionType readPermission, PermissionType writePermission) {
        this.title = title;
        this.description = description;
        this.schedule = schedule;
        this.readPermission = (readPermission != null) ? readPermission : PermissionType.PUBLIC_READ;
        this.writePermission = (writePermission != null) ? writePermission : PermissionType.MEMBERS_WRITE;
    }

    public void update(String title, String description, PermissionType readPermission, PermissionType writePermission) {
        this.title = title;
        this.description = description;
        this.readPermission = readPermission;
        this.writePermission = writePermission;
    }
}

package com.jeja.jejabe.form.domain;

import com.jeja.jejabe.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FormAccess extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private FormTemplate template;

    @Enumerated(EnumType.STRING)
    private AccessType accessType;

    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    private String targetValue; // "ROLE_LEADER", "1", "ALL"

    @Builder
    public FormAccess(AccessType accessType, TargetType targetType, String targetValue) {
        this.accessType = accessType;
        this.targetType = targetType;
        this.targetValue = targetValue;
    }
}

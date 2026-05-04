package io.envio.core.domain.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Encrypted_keys")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userdevice_project_id")
    private Long userProjectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userdevice_id")
    private UserDevice userDevice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "encrypted_key")
    private String encryptedKey; // 해당 유저의 공개키로 래핑된 프로젝트 키

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

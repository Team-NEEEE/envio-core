package io.envio.core.domain.project.entity;

import java.time.LocalDateTime;

import io.envio.core.common.entity.BaseEntity;

import io.envio.core.domain.user.entity.UserDevice;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "encrypted_keys", uniqueConstraints = {
	@UniqueConstraint(name = "uk_user_device_project", columnNames = {"user_device_id", "project_id"})
})
// (유저디바이스, 프로젝트) 관계는 무조건 유니크해야함

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EncryptedKey extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "encrypted_key_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_device_id")
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
package io.envio.core.domain.project.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.envio.core.common.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "projects")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "project_id")
	private Long id;

	@Column(name = "project_name")
	private String projectName;

	@Column(name = "organization_name")
	private String organizationName;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "version_id")
	private Long versionId;

	@Version
	private Long version;

	@Column(name = "githubApp_id")
	private String githubAppId;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public void updateVersion(Long newVersionId) {
		this.versionId = newVersionId;
		this.updatedAt = LocalDateTime.now();
	}

	// UserProject와의 1:N 관계
	// 중간 테이블을 만들었음으로 이거 써도되고 안써도 댐
	@Builder.Default
	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EncryptedKey> encryptedKeys = new ArrayList<>();
}

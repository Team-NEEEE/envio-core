package io.envio.core.domain.project.entity;

import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "histories")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class History {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "histories_id")
	private Long historiesId;

	// 프로젝트와의 N:1 관계 (DB에서 project_id로 관리)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id")
	private Project project;

	@Column(name = "version_id")
	private Long versionId;

	@Column(name = "base_version_id")
	private Long baseVersionId;

	@Column(name = "user_github_id")
	private String userGithubId;

	// PostgreSQL의 JSONB 타입을 Map으로 매핑
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "encrypted_environment", columnDefinition = "jsonb")
	private Map<String, Object> encryptedEnvironment;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}

package io.envio.core.domain.project.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public record ProjectHistoryResDto(
	@JsonProperty("histories_id")
	Long historyId,

	@JsonProperty("project_id")
	Long projectId,

	@JsonProperty("version_id")
	Long versionId,

	@JsonProperty("base_version_id")
	Long baseVersionId,

	@JsonProperty("github_id")
	String githubId,

	@JsonProperty("created_at")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	LocalDateTime createdAt
) {
}

package io.envio.core.domain.project.dto.response;

import lombok.Builder;

@Builder
public record ProjectResDto(
	Long projectId,
	String projectName,
	String description,
	Long versionId
) {
}

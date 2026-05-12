package io.envio.core.domain.project.dto.response;

import lombok.Builder;

@Builder
public record ProjectPushResDto(
	String message,
	Long historyId,
	Long projectId,
	String envName,
	Long versionId,
	Long parentVersionId
) {
}

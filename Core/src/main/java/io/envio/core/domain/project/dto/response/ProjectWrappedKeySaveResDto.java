package io.envio.core.domain.project.dto.response;

import lombok.Builder;

@Builder
public record ProjectWrappedKeySaveResDto(
	String message,
	Long projectId,
	Integer updatedCount
) {
}

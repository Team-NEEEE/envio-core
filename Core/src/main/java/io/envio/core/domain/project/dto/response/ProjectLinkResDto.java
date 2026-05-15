package io.envio.core.domain.project.dto.response;

import lombok.Builder;

@Builder
public record ProjectLinkResDto(
	String message,
	ProjectLinkProjectResDto project,
	String wrappedMasterKey,
	String joinStatus
) {
}
